/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.channel

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.api.PlatformServer
import com.mcgendai.chat.common.serverChannelChat.util.Japaniser
import com.mcgendai.chat.common.serverChannelChat.util.header
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * TellControllerクラスは、プレイヤー間のテレパシーメッセージ（tellコマンド）を処理します。
 *
 * @platform Paper
 * @param platformServer プラットフォームのインスタンス
 * @param japaniser 日本語変換ユーティリティ
 */

class TellController(
    private val platformServer: PlatformServer,
    private val japaniser: Japaniser
) {
    /**
     * プレイヤーから指定されたターゲットプレイヤーへメッセージを送信します。
     *
     * @param player メッセージを送信するプレイヤー
     * @param targetName メッセージの送信先プレイヤー名
     * @param messageO 送信するメッセージ内容
     */
    fun handleTell(player: PlatformPlayer, targetName: String, messageO: String){
        val target = platformServer.getPlayer(targetName)
        if (target == null){
            player.sendMessage(header.append(Component.text("プレイヤーが存在しません！", NamedTextColor.RED)))
            return
        }
        val message = messageO.toAS()

        val timeStr = LocalTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val kana = japaniser.conv(message,true)
        val template = "&f[{player} &f-> &f{target}&f] {msg} &f(${message}&f) &7{time}"

        // テンプレートをコンポーネント対応でパース
        val tellP = Component.text().let { root ->
            // {target} はここで扱うため template 文字列の置換準備
            val parsed = template
                .replace("{player}", "{player}")
                .replace("{target}", "{target}")
                .replace("{msg}", "{msg}")
                .replace("{time}", "{time}")

            var current = root
            val regex = Regex("\\{player}|\\{target}|\\{msg}|\\{time}")
            var lastIndex = 0

            for (match in regex.findAll(parsed)) {
                val start = match.range.first
                val end = match.range.last + 1

                // 通常文字部分
                if (start > lastIndex) {
                    val plain = parsed.substring(lastIndex, start)
                    current = current.append(Component.text(plain.replace("&", "§")))
                }

                // プレースホルダー部分
                val placeholder = match.value
                current = when (placeholder) {
                    "{player}" ->
                        current.append(
                            Component.text(player.name, NamedTextColor.GRAY)
                                .hoverEvent(Component.text("クリックして返信", NamedTextColor.WHITE))
                                .clickEvent(ClickEvent.suggestCommand("/tell ${player.name} "))
                        )

                    "{target}" ->
                        current.append(
                            Component.text(target.name, NamedTextColor.GRAY)
                                .hoverEvent(Component.text("クリックして返信", NamedTextColor.WHITE))
                                .clickEvent(ClickEvent.suggestCommand("/tell ${target.name} "))
                        )

                    "{msg}" ->
                        current.append(
                            Component.text(kana, NamedTextColor.GOLD)
                        )

                    "{time}" ->
                        current.append(
                            Component.text(timeStr, NamedTextColor.GRAY)
                        )

                    else -> current
                }

                lastIndex = end
            }

            // 最後の余り部分
            if (lastIndex < parsed.length) {
                val tail = parsed.substring(lastIndex)
                current = current.append(Component.text(tail.replace("&", "§")))
            }

            current
        }

        player.sendMessage(tellP)
        target.sendMessage(tellP)
    }

    fun String.toAS(): String {
        return this
            .replace("&0","§0")
            .replace("&1","§1")
            .replace("&2","§2")
            .replace("&3","§3")
            .replace("&4","§4")
            .replace("&5","§5")
            .replace("&6","§6")
            .replace("&7","§7")
            .replace("&8","§8")
            .replace("&9","§9")
            .replace("&a","§a")
            .replace("&b","§b")
            .replace("&c","§c")
            .replace("&d","§d")
            .replace("&e","§e")
            .replace("&f","§f")
            .replace("&k","§k")
            .replace("&l","§l")
            .replace("&m","§m")
            .replace("&n","§n")
            .replace("&o","§o")
            .replace("&r","§r")
    }
}