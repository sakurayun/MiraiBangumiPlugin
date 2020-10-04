package com.ssttkkl.mirai.bangumiplugin.command

import com.ssttkkl.mirai.bangumiplugin.MiraiBangumiPlugin
import com.ssttkkl.mirai.bangumiplugin.msgmaker.SeasonListMessageMaker
import com.ssttkkl.mirai.bangumiplugin.msgmaker.SeasonMessageMaker
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.UserCommandSender

private const val DESCRIPTION = "番剧更新提醒插件调试命令"

object BangumiDebugCommand : CompositeCommand(
    MiraiBangumiPlugin,
    "bangumi-debug",
    description = DESCRIPTION,
    prefixOptional = true
) {
    @SubCommand
    @Description("查看下一部更新的番剧")
    suspend fun CommandSender.peek() {
        val next = MiraiBangumiPlugin.seasonProducer.peek()
        val msg = if (this is UserCommandSender)
            SeasonMessageMaker.make(next)
        else
            SeasonMessageMaker.makePlain(next)
        sendMessage(msg)
    }

    @SubCommand
    @Description("查看完整番剧时间表（13天）")
    suspend fun CommandSender.all() {
        val timeline = MiraiBangumiPlugin.seasonProducer.getAll()
        val msg = SeasonListMessageMaker.make(timeline)
        sendMessage(msg)
    }

    @SubCommand
    @Description("立即从远程拉取番剧数据")
    suspend fun CommandSender.fetch() {
        MiraiBangumiPlugin.seasonProducer.forceFetch()
        sendMessage("OK")
    }

    @SubCommand
    @Description("查看所有即将更新的番剧")
    suspend fun CommandSender.pending() {
        val pending = MiraiBangumiPlugin.seasonProducer.getPending()
        val msg = SeasonListMessageMaker.make(pending)
        sendMessage(msg)
    }

    @SubCommand
    @Description("立即推送下一部更新的番剧（即使未到时间）")
    suspend fun CommandSender.deliver() {
        MiraiBangumiPlugin.seasonProducer.deliver()
        sendMessage("OK")
    }

    @SubCommand("delivertimeline")
    @Description("立即推送番剧时间表（即使未到时间）")
    suspend fun CommandSender.deliverTimeline() {
        MiraiBangumiPlugin.timelineProducer.deliver()
        sendMessage("OK")
    }
}