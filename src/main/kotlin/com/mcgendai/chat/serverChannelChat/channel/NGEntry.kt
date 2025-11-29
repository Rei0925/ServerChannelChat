/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.serverChannelChat.channel

data class NGEntry(
    val id: Int,
    val value: String,
    val severity: Int,
    val usageCount: Int
)