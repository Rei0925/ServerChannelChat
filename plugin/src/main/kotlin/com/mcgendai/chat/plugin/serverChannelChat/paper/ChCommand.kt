/****
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.paper

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.channel.ChController
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * /ch コマンドをbrigadierで実装
 *
 * @platform Paper
 * @property chController
 * @since 0.0.2
 */
class ChCommand(
    private val chController: ChController
) {
    fun register(plugin: Plugin) {
        val buildCommand: LiteralCommandNode<CommandSourceStack> = Commands.literal("ch")
            .then(
                Commands.literal("ngword")
                    .requires { sender ->
                        val player = sender.sender as? Player ?: return@requires false
                        player.isOp || player.hasPermission("scc.ngword")
                    }
                    .then(
                        Commands.literal("add")
                            .requires { sender ->
                                val player = sender.sender as? Player ?: return@requires false
                                player.isOp || player.hasPermission("scc.ngword.add")
                            }
                            .then(
                                Commands.argument(
                                    "word", StringArgumentType.string()
                                ).then(
                                    Commands.argument(
                                        "severity", IntegerArgumentType.integer(1, 4)
                                    ).executes { ctx ->
                                        val bukkitSender = ctx.source.sender as Player
                                        val sender: PlatformPlayer = PaperPlayer(bukkitSender)
                                        val word = StringArgumentType.getString(ctx, "word")
                                        val severity = IntegerArgumentType.getInteger(ctx, "severity")
                                        chController.ngWordAdd(sender,word,severity)
                                        0
                                    }
                                )
                            )
                    )
                    .then(
                        Commands.literal("del")
                            .requires { sender ->
                                val player = sender.sender as? Player ?: return@requires false
                                player.isOp || player.hasPermission("scc.ngword.del")
                            }
                            .then(
                                Commands.argument(
                                    "word", StringArgumentType.string()
                                ).executes { ctx ->
                                    val bukkitSender = ctx.source.sender as Player
                                    val sender: PlatformPlayer = PaperPlayer(bukkitSender)
                                    val word = StringArgumentType.getString(ctx, "word")
                                    chController.ngWordDel(sender, word)
                                    Command.SINGLE_SUCCESS
                                }
                            )
                    )
                    .then(
                        Commands.literal("list")
                            .requires { sender ->
                                val player = sender.sender as? Player ?: return@requires false
                                player.isOp || player.hasPermission("scc.ngword.list")
                            }
                            .executes { ctx ->
                                val bukkitSender = ctx.source.sender as Player
                                val sender: PlatformPlayer = PaperPlayer(bukkitSender)
                                chController.ngWordList(sender)
                                Command.SINGLE_SUCCESS
                            }
                    )
                    .then(
                        Commands.literal("search")
                            .requires { sender ->
                                val player = sender.sender as? Player ?: return@requires false
                                player.isOp || player.hasPermission("scc.ngword.search")
                            }
                            .then(
                                Commands.argument(
                                    "word", StringArgumentType.string()
                                ).executes { ctx ->
                                    val bukkitSender = ctx.source.sender as Player
                                    val sender: PlatformPlayer = PaperPlayer(bukkitSender)
                                    val word = StringArgumentType.getString(ctx, "word")
                                    chController.ngWordSearch(sender, word)
                                    Command.SINGLE_SUCCESS
                                }
                            )
                    )

            )
            .then(
                Commands.literal("create")
                    .requires { sender ->
                        val player = sender.sender as? Player ?: return@requires false
                        player.isOp || player.hasPermission("scc.channel.create")
                    }
                    .then(
                        Commands.argument("channelName", StringArgumentType.word())
                            .executes { ctx ->
                                val bukkitSender = ctx.source.sender as Player
                                val sender: PlatformPlayer = PaperPlayer(bukkitSender)
                                val channelName = StringArgumentType.getString(ctx, "channelName")
                                chController.createChannel(sender, channelName)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            .then(
                Commands.literal("remove")
                    .requires { sender ->
                        val player = sender.sender as? Player ?: return@requires false
                        player.isOp || player.hasPermission("scc.channel.remove")
                    }
                    .then(
                        Commands.argument("channelName", StringArgumentType.word())
                            .executes { ctx ->
                                val bukkitSender = ctx.source.sender as Player
                                val sender: PlatformPlayer = PaperPlayer(bukkitSender)
                                val channelName = StringArgumentType.getString(ctx, "channelName")
                                chController.deleteChannel(sender, channelName)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            .then(
                Commands.literal("join")
                    .requires { sender ->
                        val player = sender.sender as? Player ?: return@requires false
                        player.isOp || player.hasPermission("scc.channel.join")
                    }
                    .then(
                        Commands.argument("channelName", StringArgumentType.word())
                            .executes { ctx ->
                                val bukkitSender = ctx.source.sender as Player
                                val sender: PlatformPlayer = PaperPlayer(bukkitSender)
                                val channelName = StringArgumentType.getString(ctx, "channelName")
                                chController.join(sender, channelName)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            .then(
                Commands.literal("g")
                    .executes { ctx ->
                        val bukkitSender = ctx.source.sender as Player
                        val sender: PlatformPlayer = PaperPlayer(bukkitSender)
                        chController.join(sender, "G")
                        0
                    }
            )
            .build()

        plugin.lifecycleManager.registerEventHandler(
            LifecycleEvents.COMMANDS,
            LifecycleEventHandler { commands: ReloadableRegistrarEvent<Commands> ->
                commands.registrar().register(buildCommand)
            }
        )
    }
}