/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.bungee

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.channel.Formatter
import com.mcgendai.chat.common.serverChannelChat.util.CheckNG
import com.mcgendai.chat.common.serverChannelChat.util.Japaniser
import com.mcgendai.chat.common.serverChannelChat.util.NGwarn
import com.mcgendai.chat.common.serverChannelChat.util.db.LoggerDB
import com.mcgendai.chat.plugin.serverChannelChat.ServerChannelChatBungee
import com.mcgendai.chat.plugin.serverChannelChat.Setting
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ChatListener(
    private val plugin: ServerChannelChatBungee,
    private val japaniser: Japaniser,
    private val setting: Setting,
    private val loggerDB: LoggerDB,
    private val checkNG: CheckNG
) : Listener {

    data class WebMeta(
        val title: String?,
        val description: String?
    )

    /**
     * 初参加時にユーザーDBへ登録
     */
    @EventHandler
    fun onJoin(e: PostLoginEvent) {
        val player = BungeePlayer(e.player)
        val userDB = plugin.userDB

        if (!userDB.exists(player.uuid)) {
            userDB.insertUser(player.uuid, "G", emptyList())
        }
    }

    /**
     * チャットフック (Bungee)
     */
    @EventHandler
    fun onChat(e: ChatEvent) {
        if (e.isCommand) return
        val sender = e.sender
        if (sender !is ProxiedPlayer) return

        e.isCancelled = true

        val player: PlatformPlayer = BungeePlayer(sender)
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

        val timeStr = LocalTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))

        val chU = plugin.userDB.getCurrentChannel(player.uuid) ?: "G"
        val ch = plugin.channelDB.get(chU) ?: return

        val formattedText = Formatter().format(
            templateA = ch.format.template,
            player = player,
            worldName = sender.server.info.name,
            timeStr = timeStr,
            messageA = messageContent,
            channelName = ch.name,
            japaniser = japaniser
        )

        val legacy = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(formattedText)
        val formattedComponent = TextComponent.fromLegacyText(legacy)

        // チャンネルに応じて送信
        if (ch.isGlobalChannel) {
            ProxyServer.getInstance().players.forEach {
                it.sendMessage(ChatMessageType.CHAT, *formattedComponent)
            }
        } else {
            ProxyServer.getInstance().players
                .filter { ch.member?.contains(it.uniqueId) == true }
                .forEach { it.sendMessage(ChatMessageType.CHAT, *formattedComponent) }
        }

        // --- URLメタ情報取得 ---
        val urlRegex = """(https?://\S+)""".toRegex()
        val urls = urlRegex.findAll(messageContent).map { it.value }.toList()

        if (urls.isNotEmpty()) {
            ProxyServer.getInstance().scheduler.runAsync(plugin) {
                for (url in urls) {
                    val meta = fetchMeta(url)
                    val title = meta.title ?: continue

                    val metaMsg = buildString {
                        append("§6URL Info: §e$title")
                        if (meta.description != null) {
                            append("\n§7 - ${meta.description}")
                        }
                    }

                    val comp = TextComponent.fromLegacyText(metaMsg)

                    ProxyServer.getInstance().scheduler.runAsync(plugin) {
                        if (ch.isGlobalChannel) {
                            ProxyServer.getInstance().players.forEach {
                                it.sendMessage(*comp)
                            }
                        } else {
                            ProxyServer.getInstance().players
                                .filter { ch.member?.contains(it.uniqueId) == true }
                                .forEach { it.sendMessage(*comp) }
                        }
                    }
                }
            }
        }

        // --- ログ保存 ---
        if (setting.log) {
            ProxyServer.getInstance().scheduler.runAsync(plugin) {
                loggerDB.insertLog(ch.name, sender.uniqueId, System.currentTimeMillis(), messageContent, false)
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
            val desc = doc.select("meta[name=description]").attr("content").ifEmpty { null }

            WebMeta(title, desc)
        } catch (e: IOException) {
            WebMeta(null, null)
        }
    }
}