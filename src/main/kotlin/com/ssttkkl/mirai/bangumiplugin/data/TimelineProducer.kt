package com.ssttkkl.mirai.bangumiplugin.data

import com.ssttkkl.mirai.bangumiplugin.MiraiBangumiPlugin
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.coroutines.CoroutineContext

class TimelineProducer private constructor(
    override val coroutineContext: CoroutineContext,
    var notifyTime: LocalTime,
    private val channel: Channel<List<Season>>,
    private val getTimeline: suspend (time: LocalDateTime) -> List<Season>
) : CoroutineScope, ReceiveChannel<List<Season>> by channel {

    constructor(
        coroutineContext: CoroutineContext,
        notifyTime: LocalTime = LocalTime.of(0, 0, 0, 0),
        getTimeline: suspend (time: LocalDateTime) -> List<Season>
    ) : this(coroutineContext, notifyTime, Channel(16), getTimeline)

    private val signal = Channel<Unit>(16)

    private val ticker = launch(start = CoroutineStart.LAZY) {
        try {
            while (true) {
                val now = LocalDateTime.now()
                var nextNotify = notifyTime.atDate(now.toLocalDate())
                if (nextNotify < now) {
                    nextNotify = nextNotify.plusDays(1)
                }

                val sec = nextNotify.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC)
                MiraiBangumiPlugin.logger.info("下一次时间表播报将在 ${sec}s 后进行")
                delay(1000 * sec)

                signal.send(Unit)
            }
        } catch (exc: Exception) {
            if (exc !is CancellationException) {
                MiraiBangumiPlugin.logger.error(exc)
            } else {
                throw exc
            }
        }
    }

    suspend fun deliver() {
        signal.send(Unit)
    }

    private val notifier = launch(start = CoroutineStart.LAZY) {
        try {
            for (u in signal) {
                val now = LocalDateTime.now()
                val ss = getTimeline(now)
                channel.send(ss)
            }
        } catch (exc: Exception) {
            if (exc !is CancellationException) {
                MiraiBangumiPlugin.logger.error(exc)
            } else {
                throw exc
            }
        }
    }

    fun start() {
        ticker.start()
        notifier.start()
    }

    suspend fun stopAndJoin() {
        ticker.cancelAndJoin()
        notifier.cancelAndJoin()
    }
}