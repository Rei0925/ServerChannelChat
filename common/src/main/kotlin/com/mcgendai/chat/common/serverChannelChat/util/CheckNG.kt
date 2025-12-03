/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.util

import com.mcgendai.chat.common.serverChannelChat.util.db.NGwordDB

class CheckNG(private val ngwordDB: NGwordDB) {

    fun check(message: String): Ng {
        val ngEntries = ngwordDB.getAll() // NGEntryのリストを取得
        var maskedMessage = message
        var maxSeverity: Int? = null
        var containsNG = false
        val matchedWords = mutableListOf<String>()

        for (entry in ngEntries) {
            if (maskedMessage.contains(entry.value, ignoreCase = true)) {
                containsNG = true
                ngwordDB.incrementUsage(entry.value) // 使用回数を増やす
                val maskedWord = "*".repeat(entry.value.length)
                maskedMessage = maskedMessage.replace(entry.value, maskedWord, ignoreCase = true)
                maxSeverity = maxSeverity?.coerceAtLeast(entry.severity) ?: entry.severity
                matchedWords.add(entry.value)
            }
        }

        return Ng(
            output = maskedMessage,
            severity = maxSeverity,
            word = matchedWords.joinToString(",")
        )
    }
}

data class Ng(
    val output: String,
    val severity: Int?,
    val word: String
)