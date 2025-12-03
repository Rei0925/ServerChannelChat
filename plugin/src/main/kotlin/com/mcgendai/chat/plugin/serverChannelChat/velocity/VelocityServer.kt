/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.velocity

import com.mcgendai.chat.common.serverChannelChat.api.Platform
import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.api.PlatformServer
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import java.util.*

class VelocityServer(
    private val proxy: ProxyServer
) : PlatformServer() {

    override val platform: Platform
        get() = Platform.VELOCITY

    override fun getPlayer(name: String): PlatformPlayer? {
        return proxy.getPlayer(name)
            .flatMap { Optional.of(VelocityPlayer(it)) }
            .orElse(null)
    }

    override fun getPlayer(uuid: UUID): PlatformPlayer? {
        return proxy.getPlayer(uuid)
            .flatMap { Optional.of(VelocityPlayer(it)) }
            .orElse(null)
    }

    override fun getOnlinePlayers(): List<PlatformPlayer> {
        return proxy.allPlayers.map { VelocityPlayer(it) }.toList()
    }

    override fun broadcast(message: String) {
        proxy.sendMessage(Component.text(message))
    }

    override fun broadcast(component: Component) {
        proxy.sendMessage(component)
    }
}