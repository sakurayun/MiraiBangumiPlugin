package com.ssttkkl.mirai.bangumiplugin

import kotlinx.serialization.json.Json
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import java.io.File

suspend fun main() {
    val qqAccount = Json.decodeFromString(
        QQAccount.serializer(),
        File("qq_account.json").readText()
    )

    MiraiConsoleTerminalLoader.startAsDaemon()

    MiraiBangumiPlugin.load()
    MiraiBangumiPlugin.enable()

    val bot = MiraiConsole.addBot(qqAccount.id, qqAccount.password) {
        fileBasedDeviceInfo()
    }.alsoLogin()

    MiraiConsole.job.join()
}