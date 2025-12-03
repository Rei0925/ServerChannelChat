package com.mcgendai.chat.common.serverChannelChat.api

import net.kyori.adventure.text.Component
import java.util.UUID

abstract class PlatformServer {
    abstract val platform: Platform
    abstract fun getPlayer(name: String) :PlatformPlayer?
    abstract fun getPlayer(uuid: UUID) :PlatformPlayer?
    abstract fun getOnlinePlayers(): List<PlatformPlayer>
    abstract fun broadcast(message: String)
    abstract fun broadcast(component: Component)
}