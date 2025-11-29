/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.serverChannelChat.util

import com.mcgendai.chat.serverChannelChat.util.db.NGwordDB

class CheckNG(
    private val nGwordDB: NGwordDB
) {
    fun check(input: String): String {
        var result = input
        val ngWords = nGwordDB.getNGWords()
        for (word in ngWords) {
            if (result.contains(word)) {
                nGwordDB.incrementUsage(word)
                val masked = "*".repeat(word.length)
                result = result.replace(word, masked)
            }
        }
        return result
    }
}