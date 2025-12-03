/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.velocity

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.channel.ChController
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer

class ChCommand(private val chController: ChController, private val proxy: ProxyServer) {

    fun register() {
        val chNode: LiteralCommandNode<CommandSource> = BrigadierCommand.literalArgumentBuilder("ch")
            // /ch ngword
            .then(
                BrigadierCommand.literalArgumentBuilder("ngword")
                    .requires { source -> source.hasPermission("scc.ngword") }
                    .then(
                        BrigadierCommand.literalArgumentBuilder("add")
                            .requires { source -> source.hasPermission("scc.ngword.add") }
                            .then(
                                BrigadierCommand.requiredArgumentBuilder("word", StringArgumentType.string())
                                    .then(
                                        BrigadierCommand.requiredArgumentBuilder("severity", IntegerArgumentType.integer(1, 4))
                                            .executes { ctx ->
                                                val source = ctx.source as Player
                                                val sender: PlatformPlayer = VelocityPlayer(source)
                                                val word = StringArgumentType.getString(ctx, "word")
                                                val severity = IntegerArgumentType.getInteger(ctx, "severity")
                                                chController.ngWordAdd(sender, word, severity)
                                                Command.SINGLE_SUCCESS
                                            }
                                    )
                            )
                    )
                    .then(
                        BrigadierCommand.literalArgumentBuilder("del")
                            .requires { source -> source.hasPermission("scc.ngword.del") }
                            .then(
                                BrigadierCommand.requiredArgumentBuilder("word", StringArgumentType.string())
                                    .executes { ctx ->
                                        val sender = VelocityPlayer(ctx.source as Player)
                                        val word = StringArgumentType.getString(ctx, "word")
                                        chController.ngWordDel(sender, word)
                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
                    .then(
                        BrigadierCommand.literalArgumentBuilder("list")
                            .requires { source -> source.hasPermission("scc.ngword.list") }
                            .executes { ctx ->
                                val sender = VelocityPlayer(ctx.source as Player)
                                chController.ngWordList(sender)
                                Command.SINGLE_SUCCESS
                            }
                    )
                    .then(
                        BrigadierCommand.literalArgumentBuilder("search")
                            .requires { source -> source.hasPermission("scc.ngword.search") }
                            .then(
                                BrigadierCommand.requiredArgumentBuilder("word", StringArgumentType.string())
                                    .executes { ctx ->
                                        val sender = VelocityPlayer(ctx.source as Player)
                                        val word = StringArgumentType.getString(ctx, "word")
                                        chController.ngWordSearch(sender, word)
                                        Command.SINGLE_SUCCESS
                                    }
                            )
                    )
            )
            // /ch create
            .then(
                BrigadierCommand.literalArgumentBuilder("create")
                    .requires { source -> source.hasPermission("scc.channel.create") }
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("channelName", StringArgumentType.word())
                            .executes { ctx ->
                                val sender = VelocityPlayer(ctx.source as Player)
                                val channelName = StringArgumentType.getString(ctx, "channelName")
                                chController.createChannel(sender, channelName)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            // /ch remove
            .then(
                BrigadierCommand.literalArgumentBuilder("remove")
                    .requires { source -> source.hasPermission("scc.channel.remove") }
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("channelName", StringArgumentType.word())
                            .executes { ctx ->
                                val sender = VelocityPlayer(ctx.source as Player)
                                val channelName = StringArgumentType.getString(ctx, "channelName")
                                chController.deleteChannel(sender, channelName)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            // /ch join
            .then(
                BrigadierCommand.literalArgumentBuilder("join")
                    .requires { source -> source.hasPermission("scc.channel.join") }
                    .then(
                        BrigadierCommand.requiredArgumentBuilder("channelName", StringArgumentType.word())
                            .executes { ctx ->
                                val sender = VelocityPlayer(ctx.source as Player)
                                val channelName = StringArgumentType.getString(ctx, "channelName")
                                chController.join(sender, channelName)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            // /ch g
            .then(
                BrigadierCommand.literalArgumentBuilder("g")
                    .executes { ctx ->
                        val sender = VelocityPlayer(ctx.source as Player)
                        chController.join(sender, "G")
                        Command.SINGLE_SUCCESS
                    }
            )
            .build()

        val meta = proxy.commandManager.metaBuilder("ch")
            .aliases("channel")
            .build()

        val command = BrigadierCommand(chNode)
        proxy.commandManager.register(meta, command)
    }
}