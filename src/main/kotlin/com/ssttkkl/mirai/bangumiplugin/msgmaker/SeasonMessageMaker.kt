package com.ssttkkl.mirai.bangumiplugin.msgmaker

import com.ssttkkl.mirai.bangumiplugin.GeneralConfig
import com.ssttkkl.mirai.bangumiplugin.data.Season
import com.ssttkkl.mirai.bangumiplugin.data.toFriendString
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.LightApp
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText

object SeasonMessageMaker {
    fun make(season: Season, tag: String = ""): Message {
        val type = GeneralConfig.seasonMessageType
        return when {
            type.equals("card", true) ->
                makeCard(season, tag)
            type.equals("plain", true) ->
                makePlain(season, tag)
            else ->
                error("illegal seasonMessageType value: $type")
        }
    }

    fun makePlain(season: Season, tag: String = ""): Message {
        return PlainText(tag + " " + season.toFriendString())
    }

    fun makeCard(season: Season, tag: String = ""): Message {
        val obj = buildJsonObject {
            put("app", "com.tencent.structmsg")
            put("view", "news")
            put("ver", "0.0.0.1")
            put("desc", "新闻")
            put("prompt", season.title)
            put("meta", buildJsonObject {
                put("news", buildJsonObject {
                    put("action", "web")
                    put("android_pkg_name", "")
                    put("app_type", 0)
                    put("appid", 101793367)
                    put("desc", season.toFriendString())
                    put("jumpUrl", season.url)
                    put("preview", season.squareCover)
                    put("source_icon", "")
                    put("source_url", "")
                    put("tag", GeneralConfig.seasonCardTitle)
                    put("title", tag + season.title)
                })
            })
        }
        return LightApp(obj.toString())
    }
}