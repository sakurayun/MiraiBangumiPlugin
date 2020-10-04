package com.ssttkkl.mirai.bangumiplugin.command

import com.ssttkkl.mirai.bangumiplugin.MiraiBangumiPlugin
import com.ssttkkl.mirai.bangumiplugin.BangumiNotify
import com.ssttkkl.mirai.bangumiplugin.GeneralConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand

private const val DESCRIPTION = "管理番剧更新的提醒"

object BangumiNotifyCommand : CompositeCommand(
    MiraiBangumiPlugin,
    "bangumi-notify",
    description = DESCRIPTION,
    prefixOptional = true
) {
    private val mutex = Mutex()

    @SubCommand
    @Description("为指定Bot添加提醒目标")
    suspend fun CommandSender.add(bot: Bot, targetType: String, targetId: Long) {
        mutex.withLock {
            var noti = GeneralConfig.notifies.getOrElse(bot.id) { BangumiNotify() }

            when {
                targetType.equals("group", true) -> {
                    if (targetId in noti.notifyGroupList) {
                        sendMessage("group $targetId is already in bot ${bot.id}'s notify list")
                    } else {
                        noti = BangumiNotify(
                            notifyFriendList = noti.notifyFriendList,
                            notifyGroupList = noti.notifyGroupList + targetId
                        )
                    }
                }
                targetType.equals("friend", true) -> {
                    if (targetId in noti.notifyFriendList) {
                        sendMessage("friend $targetId is already in bot ${bot.id}'s notify list")
                    } else {
                        noti = BangumiNotify(
                            notifyFriendList = noti.notifyFriendList + targetId,
                            notifyGroupList = noti.notifyGroupList
                        )
                    }
                }
                else -> sendMessage("illegal targetType [$targetType]")
            }

            GeneralConfig.notifies = GeneralConfig.notifies + (bot.id to noti)
            sendMessage("OK")
        }
    }

    @SubCommand("remove", "rm")
    @Description("为指定Bot移除提醒目标")
    suspend fun CommandSender.remove(bot: Bot, targetType: String, targetId: Long) {
        mutex.withLock {
            var noti = GeneralConfig.notifies.getOrElse(bot.id) { BangumiNotify() }

            when {
                targetType.equals("group", true) -> {
                    if (targetId !in noti.notifyGroupList) {
                        sendMessage("group $targetId is not in bot ${bot.id}'s notify list")
                    } else {
                        noti = BangumiNotify(
                            notifyFriendList = noti.notifyFriendList,
                            notifyGroupList = noti.notifyGroupList - targetId
                        )
                    }
                }
                targetType.equals("friend", true) -> {
                    if (targetId !in noti.notifyFriendList) {
                        sendMessage("friend $targetId is not in bot ${bot.id}'s notify list")
                    } else {
                        noti = BangumiNotify(
                            notifyFriendList = noti.notifyFriendList - targetId,
                            notifyGroupList = noti.notifyGroupList
                        )
                    }
                }
                else -> sendMessage("illegal targetType [$targetType]")
            }

            GeneralConfig.notifies = GeneralConfig.notifies + (bot.id to noti)
            sendMessage("OK")
        }
    }

    @SubCommand
    @Description("查看指定Bot的提醒目标")
    suspend fun CommandSender.list(bot: Bot) {
        mutex.withLock {
            val noti = GeneralConfig.notifies.getOrElse(bot.id) { BangumiNotify() }
            sendMessage(buildString {
                appendLine("Group: ")
                noti.notifyGroupList.forEach {
                    append("- ")
                    appendLine(it)
                }
                appendLine("Friend: ")
                noti.notifyFriendList.forEach {
                    append("- ")
                    appendLine(it)
                }
            })
        }
    }
}