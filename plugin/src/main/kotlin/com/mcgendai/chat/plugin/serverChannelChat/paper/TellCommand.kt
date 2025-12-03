/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.paper

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.channel.TellController
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class TellCommand(
    private val tellController: TellController
) {
    fun register(plugin: Plugin) {
        val tellCommand = Commands.literal("tell")
            .then(
                Commands.argument("target", StringArgumentType.word())
                    .suggests { ctx, builder ->
                        val input = ctx.input.substringAfterLast(' ')
                        org.bukkit.Bukkit.getOnlinePlayers()
                            .map { it.name }
                            .filter { it.startsWith(input, ignoreCase = true) }
                            .forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                    .then(
                        Commands.argument("message", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val bukkitSender = ctx.source.sender as Player
                                val sender: PlatformPlayer = PaperPlayer(bukkitSender)

                                val targetName = StringArgumentType.getString(ctx, "target")
                                val message = StringArgumentType.getString(ctx, "message")
                                tellController.handleTell(sender, targetName, message)
                                1
                            }
                    )
            )
            .build()

        val msgCommand = Commands.literal("msg")
            .then(
                Commands.argument("target", StringArgumentType.word())
                    .suggests { ctx, builder ->
                        val input = ctx.input.substringAfterLast(' ')
                        org.bukkit.Bukkit.getOnlinePlayers()
                            .map { it.name }
                            .filter { it.startsWith(input, ignoreCase = true) }
                            .forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                    .then(
                        Commands.argument("message", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val bukkitSender = ctx.source.sender as Player
                                val sender: PlatformPlayer = PaperPlayer(bukkitSender)

                                val targetName = StringArgumentType.getString(ctx, "target")
                                val message = StringArgumentType.getString(ctx, "message")
                                tellController.handleTell(sender, targetName, message)
                                1
                            }
                    )
            )
            .build()

        val wCommand = Commands.literal("w")
            .then(
                Commands.argument("target", StringArgumentType.word())
                    .suggests { ctx, builder ->
                        val input = ctx.input.substringAfterLast(' ')
                        org.bukkit.Bukkit.getOnlinePlayers()
                            .map { it.name }
                            .filter { it.startsWith(input, ignoreCase = true) }
                            .forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                    .then(
                        Commands.argument("message", StringArgumentType.greedyString())
                            .executes { ctx ->
                                val bukkitSender = ctx.source.sender as Player
                                val sender: PlatformPlayer = PaperPlayer(bukkitSender)

                                val targetName = StringArgumentType.getString(ctx, "target")
                                val message = StringArgumentType.getString(ctx, "message")
                                tellController.handleTell(sender, targetName, message)
                                1
                            }
                    )
            )
            .build()

        plugin.lifecycleManager.registerEventHandler(
            LifecycleEvents.COMMANDS,
            LifecycleEventHandler { commands: ReloadableRegistrarEvent<Commands> ->
                commands.registrar().register(tellCommand)
                commands.registrar().register(msgCommand)
                commands.registrar().register(wCommand)
            }
        )
    }
}