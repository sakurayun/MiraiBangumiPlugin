package com.ssttkkl.mirai.bangumiplugin.data


import kotlinx.serialization.Serializable

@Serializable
data class BilibiliTimeline(
    val code: Int = 0,
    val message: String = "",
    val result: List<Result> = emptyList()
)