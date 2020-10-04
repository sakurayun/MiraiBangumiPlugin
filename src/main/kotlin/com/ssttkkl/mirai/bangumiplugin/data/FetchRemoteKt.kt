package com.ssttkkl.mirai.bangumiplugin.data

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val BILIBILI_TIMETABLE = "https://bangumi.bilibili.com/web_api/timeline_global"

suspend fun fetchRemote(): List<Season> = withContext(Dispatchers.IO) {
    val content = HttpClient().use { client ->
        client.get<ByteArray>(BILIBILI_TIMETABLE).decodeToString()
    }

    val timeline = Json.decodeFromString(BilibiliTimeline.serializer(), content)
    if (timeline.code != 0) {
        error(timeline.result)
    } else {
        timeline.result.flatMap { it.seasons }.sortedBy { it.pubTime }
    }
}