package com.ssttkkl.mirai.bangumiplugin.data


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class Result(
    @SerialName("date")
    val dateStr: String = "",
    @SerialName("date_ts")
    val dateTs: Long = 0,
    @SerialName("day_of_week")
    val dayOfWeek: Int = 0,
    @SerialName("is_today")
    val isToday: Int = 0,
    val seasons: List<Season> = listOf()
) {
    @Transient
    val date = LocalDate.ofInstant(
        Instant.ofEpochSecond(dateTs),
        ZoneId.systemDefault()
    )
}