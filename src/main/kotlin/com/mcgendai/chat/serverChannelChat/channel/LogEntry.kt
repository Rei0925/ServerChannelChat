/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.serverChannelChat.channel

import java.util.UUID

data class LogEntry(
    val id: Int,
    val channelName: String,
    val playerUuid: UUID,
    val time: Long,
    val content: String,
    val ngWord: Boolean
)