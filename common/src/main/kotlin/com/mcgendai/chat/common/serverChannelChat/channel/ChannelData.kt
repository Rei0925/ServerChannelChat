/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.channel

import java.util.UUID

/**
 * チャンネルのデータクラス
 *
 * @property name チャンネルネーム
 * @property uuid チャンネル固有のUUID
 * @property isGlobalChannel グローバルチャンネルかどうか
 * @property owner チャンネルのオーナー?
 * @property member チャンネルのメンバー?
 * @property password パスワード?
 * @property format チャンネルのフォーマット
 * @property isNGBypass NGワードをバイパスするかどうか
 * @since 0.0.1
 */
data class ChannelData(
    val name: String,
    val uuid: UUID,
    val isGlobalChannel: Boolean,
    val owner: UUID?,
    val member: List<UUID>?,
    val password: String?,
    val format: ChatFormat,
    val isNGBypass: Boolean
)