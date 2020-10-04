package com.ssttkkl.mirai.bangumiplugin

import kotlinx.serialization.Serializable

@Serializable
data class QQAccount(
    val id: Long,
    val password: String
)