/****
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.md_5.bungee.api.connection.ProxiedPlayer
import com.mcgendai.chat.common.serverChannelChat.channel.ChController

@CommandAlias("ch")
@Description("Channel main command")
class ChCommand(
    private val chController: ChController
) : BaseCommand() {

    // ------------------------------------------------------
    // NG Word Commands
    // ------------------------------------------------------

    @Subcommand("ngword add")
    @CommandPermission("scc.ngword.add")
    fun onAdd(sender: ProxiedPlayer, word: String, severity: Int) {
        val platform = BungeePlayer(sender)
        chController.ngWordAdd(platform, word, severity)
    }

    @Subcommand("ngword del")
    @CommandPermission("scc.ngword.del")
    fun onDel(sender: ProxiedPlayer, word: String) {
        val platform = BungeePlayer(sender)
        chController.ngWordDel(platform, word)
    }

    @Subcommand("ngword list")
    @CommandPermission("scc.ngword.list")
    fun onList(sender: ProxiedPlayer) {
        val platform = BungeePlayer(sender)
        chController.ngWordList(platform)
    }

    @Subcommand("ngword search")
    @CommandPermission("scc.ngword.search")
    fun onSearch(sender: ProxiedPlayer, word: String) {
        val platform = BungeePlayer(sender)
        chController.ngWordSearch(platform, word)
    }

    // ------------------------------------------------------
    // Channel Management
    // ------------------------------------------------------

    @Subcommand("create")
    @CommandPermission("scc.channel.create")
    fun onCreate(sender: ProxiedPlayer, channelName: String) {
        val platform = BungeePlayer(sender)
        chController.createChannel(platform, channelName)
    }

    @Subcommand("remove")
    @CommandPermission("scc.channel.remove")
    fun onRemove(sender: ProxiedPlayer, channelName: String) {
        val platform = BungeePlayer(sender)
        chController.deleteChannel(platform, channelName)
    }

    @Subcommand("join")
    @CommandPermission("scc.channel.join")
    fun onJoin(sender: ProxiedPlayer, channelName: String) {
        val platform = BungeePlayer(sender)
        chController.join(platform, channelName)
    }

    // g はショートカット
    @Subcommand("g")
    fun onGlobal(sender: ProxiedPlayer) {
        val platform = BungeePlayer(sender)
        chController.join(platform, "G")
    }
}