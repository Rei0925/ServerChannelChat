/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * GoogleIME
 *
 * WebのGoogle翻訳APIを利用してひらがな文字列を漢字変換します。
 */
class GoogleIME {

    private val gson = Gson()

    /**
     * ひらがなを漢字変換して返す
     * @param text 変換したいひらがな文字列
     * @return 変換結果の文字列（漢字含む）
     * @since 0.0.1
     */
    fun convert(text: String): String {
        try {
            val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())

            // URL(String) コンストラクタで URL を作成
            val url = URL("http://www.google.com/transliterate?langpair=ja-Hira|ja&text=$encoded")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            val response = conn.inputStream.bufferedReader().use { it.readText() }

            val jsonArray = gson.fromJson(response, JsonArray::class.java)
            return jsonArray.joinToString("") { entry ->
                val candidates = entry.asJsonArray[1].asJsonArray
                candidates[0].asString
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return text
        }
    }
}