/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.velocity

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import java.util.UUID

class VelocityPlayer(private val player: Player) : PlatformPlayer() {

    override val name: String
        get() = player.username

    override val uuid: UUID
        get() = player.uniqueId

    override fun sendMessage(message: Component) {
        player.sendMessage(message)
    }

    override fun sendMessage(message: TextComponent.Builder) {
        player.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun isOnline(): Boolean {
        return player.isActive
    }

    // Velocity 特有の処理
    override fun kick(reason: Component) = player.disconnect(reason)
}