package com.mcgendai.chat.common.serverChannelChat.api

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import java.util.UUID

abstract class PlatformPlayer {
    abstract val name: String
    abstract val uuid: UUID

    abstract fun sendMessage(message: Component)
    abstract fun sendMessage(message: TextComponent.Builder)
    abstract fun hasPermission(permission: String): Boolean
    abstract fun isOnline(): Boolean
    abstract fun kick(reason: Component)
}