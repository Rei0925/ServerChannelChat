/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.serverChannelChat.paper

import com.mcgendai.chat.serverChannelChat.util.db.NGwordDB
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

/**
 * CHのコマンドコントローラー
 *
 * @property nGwordDB
 * @since 0.0.2
 */
class ChController(
    private val nGwordDB: NGwordDB
) {
    val header: Component = MiniMessage.miniMessage().deserialize("<white><bold>[<gradient:#00d0ff:#0078b5>ServerChannelChat</gradient>]</bold></white>")
    fun ngWordAdd(player: Player, word: String, severity: Int) {
        nGwordDB.insert(word,severity)
        player.sendMessage(header
            .append(Component.text("NGワード「${word}」の追加を重大度「${severity}」で行いました", NamedTextColor.WHITE))
        )
    }

    fun ngWordDel(player: Player, word: String) {
        val list = nGwordDB.search(word)
        if (list.isEmpty()) {
            player.sendMessage(header.append(Component.text("NGワード「${word}」は見つかりませんでした", NamedTextColor.RED)))
            return
        }
        var deletedCount = 0
        for (ngword in list) {
            if (ngword.value == word) {
                nGwordDB.delete(ngword.id)
                deletedCount++
            }
        }
        if (deletedCount > 0) {
            player.sendMessage(header.append(Component.text("NGワード「${word}」を${deletedCount}件削除しました", NamedTextColor.WHITE)))
        } else {
            player.sendMessage(header.append(Component.text("NGワード「${word}」は見つかりませんでした", NamedTextColor.RED)))
        }
    }

    fun ngWordList(player: Player) {
        val allWords = nGwordDB.getAll()
        if (allWords.isEmpty()) {
            player.sendMessage(header.append(Component.text("登録されているNGワードはありません", NamedTextColor.YELLOW)))
            return
        }
        player.sendMessage(header.append(Component.text("登録されているNGワード一覧:", NamedTextColor.WHITE)))
        for (ngword in allWords) {
            player.sendMessage(Component.text("・${ngword.value}（重大度: ${ngword.severity}）", NamedTextColor.WHITE))
        }
    }

    fun ngWordSearch(player: Player, word: String) {
        val results = nGwordDB.search(word)
        if (results.isEmpty()) {
            player.sendMessage(header.append(Component.text("「${word}」を含むNGワードは見つかりませんでした", NamedTextColor.RED)))
            return
        }
        player.sendMessage(header.append(Component.text("「${word}」を含むNGワード一覧:", NamedTextColor.WHITE)))
        for (ngword in results) {
            player.sendMessage(Component.text("・${ngword.value}（重大度: ${ngword.severity}）", NamedTextColor.WHITE))
        }
    }
}