package com.ssttkkl.mirai.bangumiplugin.msgmaker

import com.ssttkkl.mirai.bangumiplugin.GeneralConfig
import com.ssttkkl.mirai.bangumiplugin.data.Season
import com.ssttkkl.mirai.bangumiplugin.data.toFriendString
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object SeasonListMessageMaker {
    fun make(seasons: List<Season>): Message = PlainText(
        seasons.groupBy { it.pubTime.toLocalDate() }
            .toList()
            .sortedBy { it.first }
            .joinToString("\n") { (date, list) ->
                "------${date.format(GeneralConfig.dateFormatter)}------\n" +
                        list.joinToString("\n") { it.toFriendString() }
            }
    )
}