package com.ssttkkl.mirai.bangumiplugin.command

import com.ssttkkl.mirai.bangumiplugin.GeneralConfig
import com.ssttkkl.mirai.bangumiplugin.MiraiBangumiPlugin
import com.ssttkkl.mirai.bangumiplugin.msgmaker.SeasonListMessageMaker
import com.ssttkkl.mirai.bangumiplugin.msgmaker.SeasonMessageMaker
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender
import java.time.LocalDate

private const val DESCRIPTION = "查询番剧时间表"

object BangumiCommand : SimpleCommand(
    MiraiBangumiPlugin,
    "bangumi",
    *GeneralConfig.commandSecondaryNames,
    description = DESCRIPTION,
    prefixOptional = true
) {

    @Handler
    suspend fun CommandSender.handle() {
        val today = LocalDate.now()
        val timeline = MiraiBangumiPlugin.seasonProducer.getAll().filter { it.pubTime.toLocalDate() == today }
        val msg = SeasonListMessageMaker.make(timeline)
        sendMessage(msg)
    }
}