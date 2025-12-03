/****
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.bungee

import com.mcgendai.chat.common.serverChannelChat.api.Platform
import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.api.PlatformServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.ProxyServer
import java.util.*

class BungeeServer : PlatformServer() {
    override val platform: Platform
        get() = Platform.BUNGEE

    override fun getPlayer(name: String): PlatformPlayer? {
        val player = ProxyServer.getInstance().getPlayer(name)
        return if (player != null) BungeePlayer(player) else null
    }

    override fun getPlayer(uuid: UUID): PlatformPlayer? {
        val player = ProxyServer.getInstance().getPlayer(uuid)
        return if (player != null) BungeePlayer(player) else null
    }

    override fun getOnlinePlayers(): List<PlatformPlayer> {
        return ProxyServer.getInstance().players.map { BungeePlayer(it) }
    }

    @Deprecated(
        "'fun broadcast(p0: String!): Unit' is deprecated. Deprecated in Java.",
        ReplaceWith("broadcast(Component)")
    )
    override fun broadcast(message: String) {
        ProxyServer.getInstance().broadcast(message)
    }

    override fun broadcast(component: Component) {
        ProxyServer.getInstance().broadcast(*BungeeComponentSerializer.get().serialize(component))
    }
}