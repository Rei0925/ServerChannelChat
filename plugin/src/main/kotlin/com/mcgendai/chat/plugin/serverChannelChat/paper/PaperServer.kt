package com.mcgendai.chat.plugin.serverChannelChat.paper

import com.mcgendai.chat.common.serverChannelChat.api.Platform
import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.api.PlatformServer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import java.util.UUID

class PaperServer : PlatformServer() {
    override val platform: Platform
        get() = Platform.PAPER

    override fun getPlayer(name: String): PlatformPlayer? {
        val player = Bukkit.getPlayer(name)
        return if (player != null) PaperPlayer(player) else null
    }

    override fun getPlayer(uuid: UUID): PlatformPlayer? {
        val player = Bukkit.getPlayer(uuid)
        return if (player != null) PaperPlayer(player) else null
    }

    override fun getOnlinePlayers(): List<PlatformPlayer> {
        return Bukkit.getOnlinePlayers().map { PaperPlayer(it) }
    }

    @Deprecated(
        "'static fun broadcastMessage(message: @NotNull() String): Int' is deprecated. Deprecated in Java.",
        ReplaceWith("broadcast(Component)")
    )
    override fun broadcast(message: String) {
        Bukkit.broadcastMessage(message)
    }

    override fun broadcast(component: Component) {
        Bukkit.broadcast(component)
    }
}