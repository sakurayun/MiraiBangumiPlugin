package com.ssttkkl.mirai.bangumiplugin

import com.ssttkkl.mirai.bangumiplugin.command.BangumiCommand
import com.ssttkkl.mirai.bangumiplugin.command.BangumiDebugCommand
import com.ssttkkl.mirai.bangumiplugin.command.BangumiNotifyCommand
import com.ssttkkl.mirai.bangumiplugin.data.SeasonProducer
import com.ssttkkl.mirai.bangumiplugin.data.TimelineProducer
import com.ssttkkl.mirai.bangumiplugin.data.toFriendString
import com.ssttkkl.mirai.bangumiplugin.msgmaker.SeasonListMessageMaker
import com.ssttkkl.mirai.bangumiplugin.msgmaker.SeasonMessageMaker
import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.message.data.Message
import java.time.LocalTime

const val ID = "com.ssttkkl.mirai.bangumi-plugin"
const val VERSION = "0.1.0"
const val NAME = "MiraiBangumiPlugin"
const val AUTHOR = "ssttkkl"

object MiraiBangumiPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = ID,
        version = VERSION
    ) {
        name(NAME)
        author(AUTHOR)
    }
) {
    // 定时生产Season（实现番剧更新播报）
    val seasonProducer = SeasonProducer(coroutineContext)
//    val producer = SeasonProducer(this.coroutineContext) {
//        val now = OffsetDateTime.now()
//        List(10) {
//            Season(title = "Test", pubIndex = (it + 1).toString(), pubTs = now.toEpochSecond() + (it + 1) * 60)
//        }
//    }

    // 从seasonProducer接收Season并消费（发送通知）
    private val seasonConsumer = launch(start = CoroutineStart.LAZY) {
        try {
            for (s in seasonProducer) {
                MiraiBangumiPlugin.logger.info("番剧更新：${s.toFriendString()}")
                val msg = SeasonMessageMaker.make(s, GeneralConfig.seasonNotifyTag)
                notify(msg)
            }
        } catch (exc: Exception) {
            if (exc !is CancellationException) {
                logger.error(exc)
            } else {
                throw exc
            }
        }
    }

    // 定时生产Timeline（实现每日定时播报时间表）
    val timelineProducer = TimelineProducer(coroutineContext) { dt ->
        seasonProducer.getAll().filter { it.pubTime.toLocalDate() == dt.toLocalDate() }
    }

    // 从timelineProducer接收List<Season>并消费（发送通知）
    private val timelineConsumer = launch(start = CoroutineStart.LAZY) {
        try {
            for (ss in timelineProducer) {
                MiraiBangumiPlugin.logger.info("时间表播报")
                val msg = SeasonListMessageMaker.make(ss)
                notify(msg)
            }
        } catch (exc: Exception) {
            if (exc !is CancellationException) {
                logger.error(exc)
            } else {
                throw exc
            }
        }
    }

    private suspend fun notify(msg: Message) {
        GeneralConfig.notifies.forEach { (qq, not) ->
            val bot = Bot.getInstanceOrNull(qq)
            if (bot == null) {
                logger.error("bot instance of $qq does not exists.")
                return@forEach
            }
            not.notify(bot, msg)
        }
    }

    override fun onEnable() {
        GeneralConfig.reload()

        seasonProducer.fetchIntervalMills = GeneralConfig.fetchIntervalMills
        seasonProducer.start()
        seasonConsumer.start()

        if (GeneralConfig.timelineNotifyEnabled) {
            timelineProducer.notifyTime = LocalTime.parse(GeneralConfig.timelineNotifyTime, GeneralConfig.timeFormatter)
            timelineProducer.start()
            timelineConsumer.start()
        }

        BangumiCommand.register()
        BangumiDebugCommand.register()
        BangumiNotifyCommand.register()
    }

    override fun onDisable() {
        runBlocking {
            seasonProducer.stopAndJoin()
            seasonConsumer.cancelAndJoin()

            if (GeneralConfig.timelineNotifyEnabled) {
                timelineProducer.stopAndJoin()
                timelineConsumer.cancelAndJoin()
            }
        }

        BangumiCommand.unregister()
        BangumiDebugCommand.unregister()
        BangumiNotifyCommand.unregister()
    }
}