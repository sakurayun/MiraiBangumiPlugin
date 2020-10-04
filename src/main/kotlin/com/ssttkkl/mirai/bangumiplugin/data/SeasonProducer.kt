package com.ssttkkl.mirai.bangumiplugin.data

import com.ssttkkl.mirai.bangumiplugin.MiraiBangumiPlugin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.select
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*
import kotlin.coroutines.CoroutineContext

class SeasonProducer private constructor(
    override val coroutineContext: CoroutineContext,
    var fetchIntervalMills: Long,
    private val channel: Channel<Season>,
    private val fetchRemote: suspend () -> List<Season>,
) : CoroutineScope, ReceiveChannel<Season> by channel {

    constructor(
        coroutineContext: CoroutineContext,
        fetchDurationMills: Long = 1000 * 60 * 60 * 2,
        fetchRemote: suspend () -> List<Season> = ::fetchRemote
    ) : this(coroutineContext, fetchDurationMills, Channel(16), fetchRemote)

    /*
     CSP模型实现管理all和pending。
     通过workQueue传输指令Query，由worker接收并处理指令。
     */

    // 所有的Season
    private var all: List<Season> = emptyList()

    // 即将分发的Season，队列内按照时间顺序排序
    private val pending: Deque<Season> = ArrayDeque()

    // 当更新all与pending时传输的信号，由poster接收
    private val updateSignal = Channel<Unit>(16)

    // 指令类（密封），处理完毕后将结果设置到result
    private sealed class Query<out T, U>(val attr: T) {
        val result = CompletableDeferred<U>()

        class GetAll : Query<Unit, List<Season>>(Unit)
        class GetPending : Query<Unit, List<Season>>(Unit)
        class Peek : Query<Unit, Season>(Unit)
        class Update(data: List<Season>) : Query<List<Season>, Unit>(data)
        class Deliver : Query<Unit, Unit>(Unit)
    }

    // 用于传输指令
    private val workQueue = Channel<Query<*, *>>(16)

    // 负责接收并处理workQueue中的指令
    private val worker = launch(start = CoroutineStart.LAZY) {
        for (q in workQueue) {
            try {
                when (q) {
                    is Query.GetAll -> q.result.complete(doGetAll())
                    is Query.GetPending -> q.result.complete(doGetPending())
                    is Query.Peek -> q.result.complete(doPeek())
                    is Query.Update -> q.result.complete(doUpdate(q.attr))
                    is Query.Deliver -> q.result.complete(doDeliver())
                }
            } catch (exc: Exception) {
                q.result.completeExceptionally(exc)
                if (exc is CancellationException) {
                    throw exc
                }
            }
        }
    }

    // 处理Query.GetAll指令
    private fun doGetAll() = all

    // 处理Query.GetPending指令
    private fun doGetPending() = pending.toList()

    // 处理Query.Peek指令
    private fun doPeek() = pending.first

    // 处理Query.Update指令
    private suspend fun doUpdate(data: List<Season>) {
        all = data

        val now = LocalDateTime.now()

        var begin = 0
        var end = all.size

        while (begin < end) {
            val mid = (begin + end) / 2
            if (all[mid].pubTime < now) {
                begin = mid + 1
            } else {
                end = mid
            }
        }

        pending.clear()
        pending.addAll(all.slice(begin until all.size).filter { !it.isDelay })
        updateSignal.send(Unit)

        MiraiBangumiPlugin.logger.info("拉取远程数据成功：${all.size} 部番剧，${pending.size} 部即将更新")
    }

    // 处理Query.Deliver指令
    private suspend fun doDeliver() {
        val next = pending.removeFirst()
        channel.send(next)
    }

    // 发送Query.GetAll指令并等待处理完毕
    suspend fun getAll(): List<Season> {
        val q = Query.GetAll()
        workQueue.send(q)
        return q.result.await()
    }

    // 发送Query.GetPending指令并等待处理完毕
    suspend fun getPending(): List<Season> {
        val q = Query.GetPending()
        workQueue.send(q)
        return q.result.await()
    }

    // 发送Query.peek指令并等待处理完毕
    suspend fun peek(): Season {
        val q = Query.Peek()
        workQueue.send(q)
        return q.result.await()
    }

    // 发送Query.update指令并等待处理完毕
    suspend fun update(data: List<Season>) {
        val q = Query.Update(data)
        workQueue.send(q)
        q.result.await()
    }

    // 发送Query.deliver指令并等待处理完毕
    suspend fun deliver() {
        val q = Query.Deliver()
        workQueue.send(q)
        q.result.await()
    }

    /* 管理all和pending部分结束 */

    // 负责按时分发pending中的番剧
    private val poster = launch(start = CoroutineStart.LAZY) {
        var wait = launch { delay(Long.MAX_VALUE) }

        while (true) {
            try {
                select<Unit> {
                    wait.onJoin { deliver() } // 当pending的队首到时间时进行分发操作
                    updateSignal.onReceive { } // 当数据更新时重新处理pending的队首
                }

                // 接下来继续处理pending的（新的）队首
                try {
                    val pending = peek()
                    val now = OffsetDateTime.now()
                    val sec = pending.pubTime.toEpochSecond(now.offset) - now.toEpochSecond()
                    wait = launch { delay(sec * 1000) }
                    MiraiBangumiPlugin.logger.info("下一部更新的番剧（${sec}s后）：${pending.toFriendString()}")
                } catch (_: Exception) {
                    wait = launch { delay(Long.MAX_VALUE) }
                }
            } catch (exc: Exception) {
                if (exc !is CancellationException) {
                    MiraiBangumiPlugin.logger.error(exc)
                } else {
                    throw exc
                }
            }
        }
    }

    // 要求拉取数据时传输的信号，由fetcher接收
    private val requireFetchSignal = Channel<Unit>()

    // 向requireFetchSignal发送信号，从而实现立即拉取数据的功能
    suspend fun forceFetch() {
        requireFetchSignal.send(Unit)
    }

    // 定时向requireFetchSignal发送信号，从而实现定时拉取数据的功能
    private val fetchTicker = launch(start = CoroutineStart.LAZY) {
        try {
            while (true) {
                requireFetchSignal.send(Unit)
                delay(fetchIntervalMills)
            }
        } catch (exc: Exception) {
            if (exc !is CancellationException) {
                MiraiBangumiPlugin.logger.error(exc)
            } else {
                throw exc
            }
        }
    }

    // 从requireFetchSignal接收到信号时拉取数据
    private val fetcher = launch(start = CoroutineStart.LAZY) {
        for (u in requireFetchSignal) {
            try {
                MiraiBangumiPlugin.logger.info("正在从远程拉取番剧数据……")
                val data = fetchRemote()
                update(data)
            } catch (exc: Exception) {
                if (exc !is CancellationException) {
                    MiraiBangumiPlugin.logger.error(exc)
                } else {
                    throw exc
                }
            }
        }
    }

    /* 虽然这里应该写点注释但是因为太显而易见了就懒得写了 */


    fun start() {
        worker.start()
        poster.start()
        fetcher.start()
        fetchTicker.start()
    }

    suspend fun stopAndJoin() {
        worker.cancelAndJoin()
        poster.cancelAndJoin()
        fetcher.cancelAndJoin()
        fetchTicker.cancelAndJoin()
    }

    /* 显而易见部分结束 */
}