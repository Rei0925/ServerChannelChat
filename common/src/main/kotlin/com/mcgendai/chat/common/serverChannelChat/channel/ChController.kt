/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.channel

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import com.mcgendai.chat.common.serverChannelChat.util.db.ChannelDB
import com.mcgendai.chat.common.serverChannelChat.util.db.NGwordDB
import com.mcgendai.chat.common.serverChannelChat.util.db.UserDB
import com.mcgendai.chat.common.serverChannelChat.util.header
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID
import kotlin.text.get

/**
 * CHのコマンドコントローラー
 *
 * @platform All
 * @property nGwordDB
 * @since 0.0.2
 */
class ChController(
    private val nGwordDB: NGwordDB,
    private val channelDB: ChannelDB,
    private val userDB: UserDB
) {
    fun ngWordAdd(player: PlatformPlayer, word: String, severity: Int) {
        nGwordDB.insert(word,severity)
        player.sendMessage(
            header
            .append(Component.text("NGワード「${word}」の追加を重大度「${severity}」で行いました", NamedTextColor.WHITE))
        )
    }

    fun ngWordDel(player: PlatformPlayer, word: String) {
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

    fun ngWordList(player: PlatformPlayer) {
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

    fun ngWordSearch(player: PlatformPlayer, word: String) {
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

    fun createChannel(player: PlatformPlayer, channelName: String){
        // チャンネル名チェック
        val exists = channelDB.get(channelName) != null
        if (exists) {
            player.sendMessage(
                header.append(
                    Component.text("チャンネル「${channelName}」は既に存在します", NamedTextColor.RED)
                )
            )
            return
        }
        val data = ChannelData(
            name = channelName,
            uuid = UUID.randomUUID(),
            isGlobalChannel = false,
            owner = player.uuid,
            member = listOf(player.uuid),
            password = null,
            format = ChatFormat("[{ch}]<{player}> {msg}"),
            isNGBypass = false
        )
        channelDB.insert(data)
        // ユーザーの所属チャンネル更新
        userDB.updateCurrentChannel(player.uuid, channelName)
        userDB.addChannel(player.uuid, channelName)
        player.sendMessage(
            header.append(
                Component.text("チャンネル「${channelName}」を作成しました！", NamedTextColor.AQUA)
            )
        )
    }
    fun join(player: PlatformPlayer, channelName: String){
        val channel = channelDB.get(channelName)
        if (channel == null) {
            player.sendMessage(
                header.append(
                    Component.text("チャンネル「${channelName}」は存在しません", NamedTextColor.RED)
                )
            )
            return
        }
        if (channel.isGlobalChannel){
            userDB.updateCurrentChannel(player.uuid, channelName)
            player.sendMessage(
                header.append(
                    Component.text("チャンネル「${channelName}」に切り替えました", NamedTextColor.AQUA)
                )
            )
            return
        }
        val isMember = channel.member?.contains(player.uuid)
        if (isMember == true) {
            userDB.updateCurrentChannel(player.uuid, channelName)
            player.sendMessage(
                header.append(
                    Component.text("チャンネル「${channelName}」に切り替えました", NamedTextColor.AQUA)
                )
            )
        } else {
            val updatedMembers = channel.member?.plus(player.uuid)
            val updatedChannel = channel.copy(member = updatedMembers)
            channelDB.update(updatedChannel)
            userDB.addChannel(player.uuid, channelName)
            userDB.updateCurrentChannel(player.uuid, channelName)
            player.sendMessage(
                header.append(
                    Component.text("チャンネル「${channelName}」に参加しました", NamedTextColor.AQUA)
                )
            )
        }
    }
    fun deleteChannel(player: PlatformPlayer, channelName: String) {
        val channel = channelDB.get(channelName)
        if (channel == null) {
            player.sendMessage(
                header.append(
                    Component.text("チャンネル「${channelName}」は存在しません", NamedTextColor.RED)
                )
            )
            return
        }
        // Remove channel from channelDB
        channelDB.delete(channelName)
        // Remove channel from all users' channel lists
        val allUsers = userDB.getAllUsers()
        for (user in allUsers) {
            val uuid = user.third  // Triple の 3 番目が UUID
            userDB.removeChannel(uuid, channelName)
        }
        player.sendMessage(
            header.append(
                Component.text("チャンネル「${channelName}」を削除しました", NamedTextColor.AQUA)
            )
        )
    }
}
