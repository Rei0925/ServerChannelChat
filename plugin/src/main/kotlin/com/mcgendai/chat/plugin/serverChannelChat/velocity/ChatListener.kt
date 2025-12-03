/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.velocity

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.channel.Formatter
import com.mcgendai.chat.common.serverChannelChat.util.CheckNG
import com.mcgendai.chat.common.serverChannelChat.util.Japaniser
import com.mcgendai.chat.common.serverChannelChat.util.NGwarn
import com.mcgendai.chat.common.serverChannelChat.util.YukiKanaConverter
import com.mcgendai.chat.common.serverChannelChat.util.db.LoggerDB
import com.mcgendai.chat.plugin.serverChannelChat.ServerChannelChatVelocity
import com.mcgendai.chat.plugin.serverChannelChat.Setting
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.concurrent.CompletableFuture

/**
 * Velocityのチャットをフックするクラス
 *
 * @platform Velocity
 * @property serverChannelChatVelocity
 * @property japaniser
 * @property setting
 * @property loggerDB
 * @property checkNG
 */
class ChatListener(
    private val proxyServer: ProxyServer,
    private val serverChannelChatVelocity: ServerChannelChatVelocity,
    private val japaniser: Japaniser,
    private val setting: Setting,
    private val loggerDB: LoggerDB,
    private val checkNG: CheckNG
) {

    data class WebMeta(val title: String?, val description: String?)

    @Subscribe
    fun onJoin(e: PlayerChooseInitialServerEvent) {
        val userDB = serverChannelChatVelocity.userDB
        val player = VelocityPlayer(e.player)

        if (!userDB.exists(player.uuid)) {
            userDB.insertUser(player.uuid, "G", emptyList())
        }
    }

    @Subscribe
    fun onChat(e: PlayerChatEvent) {
        e.result = PlayerChatEvent.ChatResult.denied() // デフォルトでキャンセル
        val player: PlatformPlayer = VelocityPlayer(e.player)
        val original = e.message
        val ngStatus = checkNG.check(japaniser.conv(original,true))

        when(ngStatus.severity){
            2 -> {
                NGwarn.notice(player,ngStatus.word)
            }
            3 -> {
                NGwarn.warning(player,ngStatus.word)
            }
            4 -> {
                NGwarn.kick(player,ngStatus.word)
                return
            }
            else -> return
        }
        val safe = checkNG.check(original)
        val messageContent = safe.output

        val timeStr = java.time.LocalTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
        val chU = serverChannelChatVelocity.userDB.getCurrentChannel(player.uuid) ?: "G"
        val ch = serverChannelChatVelocity.channelDB.get(chU) ?: return

        val formattedComponent = Formatter().format(
            templateA = ch.format.template,
            player = player,
            worldName = e.player.currentServer.get().serverInfo.name,
            timeStr = timeStr,
            messageA = messageContent,
            channelName = ch.name,
            japaniser = japaniser
        )

        if (ch.isGlobalChannel) {
            proxyServer.allPlayers.forEach { it.sendMessage(formattedComponent) }
        } else {
            proxyServer.allPlayers
                .filter { ch.member?.contains(it.uniqueId) == true }
                .forEach { it.sendMessage(formattedComponent) }
        }

        val urls = """(https?://\S+)""".toRegex().findAll(messageContent).map { it.value }.toList()
        if (urls.isNotEmpty()) {
            CompletableFuture.runAsync {
                urls.forEach { url ->
                    val meta = fetchMeta(url)
                    if (meta.title != null || meta.description != null) {
                        val metaComponent = Component.text()
                            .append(Component.text("URL Info: ", NamedTextColor.GOLD))
                            .append(Component.text(meta.title ?: "No Title", NamedTextColor.YELLOW))
                            .append(
                                if (meta.description != null)
                                    Component.newline()
                                        .append(Component.text(" - ${meta.description}", NamedTextColor.GRAY))
                                else
                                    Component.empty()
                            )
                            .build()

                        proxyServer.allPlayers
                            .filter { ch.isGlobalChannel || ch.member?.contains(it.uniqueId) == true }
                            .forEach { it.sendMessage(metaComponent) }
                    }
                }
            }
        }

        if (setting.log) {
            CompletableFuture.runAsync {
                loggerDB.insertLog(ch.name, e.player.uniqueId, System.currentTimeMillis(), messageContent, false)
            }
        }
    }

    private fun fetchMeta(url: String): WebMeta {
        return try {
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get()
            val title = doc.title()
            val description = doc.select("meta[name=description]").attr("content").ifEmpty { null }
            WebMeta(title, description)
        } catch (e: IOException) {
            WebMeta(null, null)
        }
    }
}