package com.ssttkkl.mirai.bangumiplugin

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.Message

@Serializable
data class BangumiNotify(
    val notifyFriendList: List<Long> = emptyList(),
    val notifyGroupList: List<Long> = emptyList()
) {
    suspend fun notify(bot: Bot, message: Message) {
        bot.friends
            .filter { it.id in notifyFriendList }
            .forEach { it.sendMessage(message) }

        bot.groups
            .filter { it.id in notifyGroupList }
            .forEach { it.sendMessage(message) }
    }
}