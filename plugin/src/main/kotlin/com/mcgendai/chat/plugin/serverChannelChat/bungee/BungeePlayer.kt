/****
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.bungee

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.UUID

class BungeePlayer(private val player: ProxiedPlayer) : PlatformPlayer() {

    override val name: String
        get() = player.name

    override val uuid: UUID
        get() = player.uniqueId

    override fun sendMessage(message: Component) {
        player.sendMessage(*BungeeComponentSerializer.get().serialize(message))
    }

    override fun sendMessage(message: TextComponent.Builder) {
        val built = message.build()
        player.sendMessage(*BungeeComponentSerializer.get().serialize(built))
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun isOnline(): Boolean {
        return player.isConnected
    }

    // Bungee 特有の処理
    override fun kick(reason: Component) = player.disconnect(*BungeeComponentSerializer.get().serialize(reason))
}