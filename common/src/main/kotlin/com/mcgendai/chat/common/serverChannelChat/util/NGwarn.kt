package com.mcgendai.chat.common.serverChannelChat.util

import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

object NGwarn {
    //重大度2
    fun notice(player: PlatformPlayer, word: String){
        player.sendMessage(header.append(Component.text("注意:", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append(Component.text("NGワード「${word}」が含まれていました！発言に気をつけてください！", NamedTextColor.WHITE)
                .decoration(TextDecoration.BOLD, false)
            )
        ))
    }
    //重大度3
    fun warning(player: PlatformPlayer, word: String){
        player.sendMessage(header.append(Component.text("警告:", NamedTextColor.RED, TextDecoration.BOLD)
            .append(Component.text("NGワード「${word}」が含まれていました！発言に気をつけてください！場合によっては然るべき対応を行う場合がございます。", NamedTextColor.WHITE)
                .decoration(TextDecoration.BOLD, false)
            )
        ))
    }
    //重大度4
    fun kick(player: PlatformPlayer, word: String){
        player.kick(header.append(Component.text("理由", NamedTextColor.DARK_RED, TextDecoration.BOLD)
            .appendNewline()
            .append(Component.text("NGワード「${word}」が含まれていたため、KICKを行いました。", NamedTextColor.WHITE)
                .decoration(TextDecoration.BOLD, false)
            )
        ))
    }
}