/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.velocity

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.channel.TellController
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.command.VelocityBrigadierMessage
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.minimessage.MiniMessage

class TellCommand(
    private val tellController: TellController,
    private val proxy: ProxyServer
) {

    fun register() {
        val node: LiteralCommandNode<CommandSource> = BrigadierCommand.literalArgumentBuilder("tell")
            .then(
                BrigadierCommand.requiredArgumentBuilder("target", StringArgumentType.word())
                    .suggests { ctx, builder ->
                        val input = ctx.input.substringAfterLast(' ')
                        proxy.allPlayers
                            .map { it.username }
                            .filter { it.startsWith(input, ignoreCase = true) }
                            .forEach { name ->
                                builder.suggest(
                                    name,
                                    VelocityBrigadierMessage.tooltip(
                                        MiniMessage.miniMessage().deserialize("<yellow>$name</yellow>")
                                    )
                                )
                            }
                        builder.buildFuture()
                    }
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("message", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val source = ctx.source
                                if (source !is com.velocitypowered.api.proxy.Player) return@executes 0
                                val sender: PlatformPlayer = VelocityPlayer(source)
                                val targetName = StringArgumentType.getString(ctx, "target")
                                val message = StringArgumentType.getString(ctx, "message")
                                tellController.handleTell(sender, targetName, message)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            .build()

        val meta = proxy.commandManager.metaBuilder("tell")
            .aliases("tell")
            .aliases("w")
            .aliases("msg")
            .build()

        val command = BrigadierCommand(node)
        proxy.commandManager.register(meta, command)
    }
}