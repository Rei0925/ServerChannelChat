package com.mcgendai.chat.plugin.serverChannelChat.paper

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import java.util.UUID

class PaperPlayer(private val player: Player) : PlatformPlayer() {

    override val name: String
        get() = player.name

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
        return player.isOnline
    }

    // Paper 特有の処理
    fun getLocation() = player.location
    override fun kick(reason: Component) = player.kick(reason)
}