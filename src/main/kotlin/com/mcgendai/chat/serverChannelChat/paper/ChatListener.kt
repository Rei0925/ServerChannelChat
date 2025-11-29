/**
 * @author     Rei09255
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.serverChannelChat.paper

import com.mcgendai.chat.serverChannelChat.ServerChannelChatPaper
import com.mcgendai.chat.serverChannelChat.Setting
import com.mcgendai.chat.serverChannelChat.channel.ChatFormat
import com.mcgendai.chat.serverChannelChat.util.CheckNG
import com.mcgendai.chat.serverChannelChat.util.Japaniser
import com.mcgendai.chat.serverChannelChat.util.db.LoggerDB

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

/**
 * Paperのチャットをフックするクラス
 *
 * @property serverChannelChatPaper
 * @property japaniser
 * @property setting
 * @property loggerDB
 * @property checkNG
 */
class ChatListener(
    private val serverChannelChatPaper: ServerChannelChatPaper,
    private val japaniser: Japaniser,
    private val setting: Setting,
    private val loggerDB: LoggerDB,
    private val checkNG: CheckNG
) : Listener {
    data class WebMeta(
        val title: String?,
        val description: String?
    )

    @EventHandler
    fun onChat(e: AsyncChatEvent) {
        if (e.isCancelled){return}

        val original = PlainTextComponentSerializer.plainText().serialize(e.message())

        val urlRegex = """(https?://\S+)""".toRegex()

        val sb = StringBuilder()
        var lastIndex = 0
        for (match in urlRegex.findAll(original)) {
            val start = match.range.first
            val end = match.range.last + 1
            if (start > lastIndex) {
                val nonUrlSegment = original.substring(lastIndex, start)
                sb.append(japaniser.conv(nonUrlSegment, true))
            }
            sb.append(original.substring(start, end))
            lastIndex = end
        }
        if (lastIndex < original.length) {
            val nonUrlSegment = original.substring(lastIndex)
            sb.append(japaniser.conv(nonUrlSegment, true))
        }
        val converted = sb.toString()

        val messageContent = checkNG.check(converted)

        val timeStr = java.time.LocalTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))

        val formattedMsgOnly = ChatFormat("&f{msg}").apply(messageContent, "", "")
        val metaText = ChatFormat(" &6@{world} &7{time}").apply("", timeStr, e.player.world.name)

        e.isCancelled = true

        val playerPrefix = Component.text("[${e.player.name}]", NamedTextColor.AQUA)

        val messageComponentBuilder = Component.text().append(playerPrefix)

        var lastIndexComp = 0
        for (match in urlRegex.findAll(formattedMsgOnly)) {
            val start = match.range.first
            val end = match.range.last + 1

            if (start > lastIndexComp) {
                messageComponentBuilder.append(Component.text(formattedMsgOnly.substring(lastIndexComp, start)))
            }

            val url = match.value
            messageComponentBuilder.append(
                Component.text(url)
                    .clickEvent(ClickEvent.openUrl(url))
                    .color(NamedTextColor.AQUA)
            )
            lastIndexComp = end
        }

        if (lastIndexComp < formattedMsgOnly.length) {
            messageComponentBuilder.append(Component.text(formattedMsgOnly.substring(lastIndexComp)))
        }

        messageComponentBuilder.append(Component.text(metaText))

        val newComponent = messageComponentBuilder.build()

        // 全員に送信
        Bukkit.getOnlinePlayers().forEach { player ->
            player.sendMessage(newComponent)
        }

        // Fetch and send metadata for each URL separately
        val urls = urlRegex.findAll(messageContent).map { it.value }.toList()
        if (urls.isNotEmpty()) {
            Bukkit.getScheduler().runTaskAsynchronously(serverChannelChatPaper, Runnable {
                for (url in urls) {
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
                        Bukkit.getScheduler().runTask(serverChannelChatPaper, Runnable {
                            Bukkit.getOnlinePlayers().forEach { player ->
                                player.sendMessage(metaComponent)
                            }
                        })
                    }
                }
            })
        }

        val now = System.currentTimeMillis()
        if (setting.log){
            Bukkit.getScheduler().runTaskAsynchronously(serverChannelChatPaper, Runnable {
                loggerDB.insertLog("G", e.player.uniqueId, now, converted, false)
            })
        }
    }

    private fun fetchMeta(url: String): WebMeta {
        return try {
            // HTMLを取得して解析
            val doc: Document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get()

            // title と meta description を抽出
            val title = doc.title()
            val description = doc.select("meta[name=description]").attr("content").ifEmpty { null }

            WebMeta(title, description)
        } catch (e: IOException) {
            // 接続失敗や解析失敗の場合は null
            WebMeta(null, null)
        }
    }
}