/**
 * @author     Rei09255
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat.paper

import com.mcgendai.chat.common.serverChannelChat.channel.Formatter
import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.util.CheckNG
import com.mcgendai.chat.common.serverChannelChat.util.Japaniser
import com.mcgendai.chat.common.serverChannelChat.util.NGwarn
import com.mcgendai.chat.common.serverChannelChat.util.db.LoggerDB
import com.mcgendai.chat.plugin.serverChannelChat.ServerChannelChatPaper
import com.mcgendai.chat.plugin.serverChannelChat.Setting

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

/**
 * Paperのチャットをフックするクラス
 *
 * @platform Paper
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

    /**
     * プレイヤー参加時にユーザーデータが無ければ作成する
     */
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val userDB = serverChannelChatPaper.userDB
        val player = PaperPlayer(e.player)

        if (!userDB.exists(player.uuid)) {
            userDB.insertUser(player.uuid,"G", emptyList())
        }
    }

    @EventHandler
    fun onChat(e: AsyncChatEvent) {
        if (e.isCancelled){return}
        e.isCancelled = true
        val player: PlatformPlayer = PaperPlayer(e.player)
        val original = PlainTextComponentSerializer.plainText().serialize(e.message())
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
        val chU = serverChannelChatPaper.userDB.getCurrentChannel(player.uuid) ?: "G"
        val ch = serverChannelChatPaper.channelDB.get(chU) ?: return

        val formattedComponent = Formatter().format(
            templateA = ch.format.template,
            player = player,
            worldName = e.player.world.name,
            timeStr = timeStr,
            messageA = messageContent,
            channelName = ch.name,
            japaniser = japaniser
        )

        // 全員に送信
        if (ch.isGlobalChannel){
            Bukkit.getOnlinePlayers().forEach { player ->
                player.sendMessage(formattedComponent)
            }
        }else{
            PaperServer().getOnlinePlayers()
                .filter { ch.member?.contains(it.uuid) == true  }
                .forEach { it.sendMessage(formattedComponent) }
        }

        // Fetch and send metadata for each URL separately
        val urlRegex = """(https?://\S+)""".toRegex()
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
                            if (ch.isGlobalChannel){
                                Bukkit.getOnlinePlayers().forEach { player ->
                                    player.sendMessage(metaComponent)
                                }
                            }else{
                                PaperServer().getOnlinePlayers()
                                    .filter { ch.member?.contains(it.uuid) == true  }
                                    .forEach { it.sendMessage(metaComponent) }
                            }
                        })
                    }
                }
            })
        }

        val now = System.currentTimeMillis()
        if (setting.log){
            Bukkit.getScheduler().runTaskAsynchronously(serverChannelChatPaper, Runnable {
                loggerDB.insertLog(ch.name, e.player.uniqueId, now, messageContent, false)
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