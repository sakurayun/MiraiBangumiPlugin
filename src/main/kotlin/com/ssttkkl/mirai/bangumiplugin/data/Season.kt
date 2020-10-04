package com.ssttkkl.mirai.bangumiplugin.data

import com.ssttkkl.mirai.bangumiplugin.GeneralConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Serializable
data class Season(
    val cover: String = "",
    val delay: Int = 0,
    @SerialName("delay_id")
    val delayId: Int = 0,
    @SerialName("delay_index")
    val delayIndex: String = "",
    @SerialName("delay_reason")
    val delayReason: String = "",
    @SerialName("ep_id")
    val epId: Int = 0,
    val favorites: Int = 0,
    val follow: Int = 0,
    @SerialName("is_published")
    val isPublished: Int = 0,
    @SerialName("pub_index")
    val pubIndex: String = "",
    @SerialName("pub_time")
    val pubTimeStr: String = "",
    @SerialName("pub_ts")
    val pubTs: Long = 0,
    @SerialName("season_id")
    val seasonId: Int = 0,
    @SerialName("season_status")
    val seasonStatus: Int = 0,
    @SerialName("square_cover")
    val squareCover: String = "",
    val title: String = "",
    val url: String = ""
) {
    @Transient
    val pubTime = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(pubTs),
        ZoneId.systemDefault()
    )

    val isDelay
        get() = delay != 0
}

fun Season.toFriendString() = if (isDelay)
    "（${delayReason}）《${title}》${pubIndex}"
else
    "（${pubTime.format(GeneralConfig.timeFormatter)}）《${title}》${pubIndex}"