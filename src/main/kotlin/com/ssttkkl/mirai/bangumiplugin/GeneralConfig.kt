package com.ssttkkl.mirai.bangumiplugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value
import java.time.format.DateTimeFormatter

object GeneralConfig : AutoSavePluginConfig("general") {
    var dateFormat by value("yyyy-MM-dd")
    var timeFormat by value("HH:mm")

    val dateFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(dateFormat)

    val timeFormatter: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern(timeFormat)

    var fetchIntervalMills by value<Long>(1000 * 60 * 60 * 2)

    var commandSecondaryNames by value(arrayOf("看看番"))

    var notifies by value<Map<Long, BangumiNotify>>(emptyMap())

    var seasonMessageType by value("card")
    var seasonNotifyTag by value("【番剧更新】")
    var seasonCardTitle by value("番剧更新提醒")

    var timelineNotifyEnabled by value(true)
    var timelineNotifyTime by value("00:00")
}