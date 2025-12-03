/****
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.bungee

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Syntax
import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.channel.TellController
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

@CommandAlias("tell")
class TellCommand(
    private val tellController: TellController
): BaseCommand(){
    @Default
    @CommandCompletion("@player")
    @Syntax("[player] [message]")
    fun tell(sender: CommandSender, target:String, message: String){
        val proxyPlayer = sender as ProxiedPlayer
        val player: PlatformPlayer  = BungeePlayer(proxyPlayer)
        tellController.handleTell(player, target, message)
    }
}