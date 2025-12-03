package com.mcgendai.chat.common.serverChannelChat.channel

import com.mcgendai.chat.common.serverChannelChat.util.Japaniser
import com.mcgendai.chat.common.serverChannelChat.api.PlatformPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor

class Formatter {
    fun format(
        templateA: String,
        player: PlatformPlayer,
        worldName: String,
        timeStr: String,
        messageA: String,
        channelName: String? = null,
        japaniser: Japaniser
    ): Component {
        val message = messageA
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
        val template = templateA
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

        val urlRegex = """https?://\S+""".toRegex()
        val components = mutableListOf<Component>()
        var index = 0
        val regex = "\\{(player|world|time|msg|ch|original)\\}".toRegex()

        fun formatMessageWithUrls(original: String): Component {
            val builder = Component.text()
            var lastIndex = 0
            for (match in urlRegex.findAll(original)) {
                val start = match.range.first
                val end = match.range.last + 1
                if (start > lastIndex) {
                    val nonUrlSegment = original.substring(lastIndex, start)
                    val conv = japaniser.conv(nonUrlSegment, true)
                    builder.append(Component.text(conv).color(NamedTextColor.GOLD))
                }
                val url = match.value
                builder.append(
                    Component.text(url)
                        .clickEvent(ClickEvent.openUrl(url))
                        .color(NamedTextColor.AQUA)
                )
                lastIndex = end
            }
            if (lastIndex < original.length) {
                val nonUrlSegment = original.substring(lastIndex)
                val conv = japaniser.conv(nonUrlSegment, true)
                builder.append(Component.text(conv).color(NamedTextColor.GOLD))
            }
            return builder.build()
        }

        for (match in regex.findAll(templateA)) {
            if (match.range.first > index) {
                val textSegment = template.substring(index, match.range.first)
                components.add(Component.text(textSegment))
            }
            when (match.groupValues[1]) {
                "player" -> {
                    val playerNameComponent = Component.text(player.name, NamedTextColor.AQUA)
                        .hoverEvent(HoverEvent.showText(Component.text("プライベートチャット")))
                        .clickEvent(ClickEvent.suggestCommand("/tell ${player.name} "))
                    components.add(playerNameComponent)
                }
                "world" -> components.add(Component.text(worldName))
                "time" -> components.add(Component.text(timeStr))
                "msg" -> components.add(formatMessageWithUrls(message))
                "original" -> components.add(Component.text(message))
                "ch" -> {
                    channelName?.let {
                        val channelComponent = Component.text(it, NamedTextColor.GREEN)
                            .hoverEvent(HoverEvent.showText(Component.text("発言先を${it}に変更")))
                            .clickEvent(ClickEvent.runCommand("/ch join $it"))
                        components.add(channelComponent)
                    } ?: components.add(formatMessageWithUrls("{ch}"))
                }
            }
            index = match.range.last + 1
        }

        if (index < templateA.length) {
            val tailSegment = template.substring(index)
            components.add(formatMessageWithUrls(tailSegment))
        }

        return Component.text().append(components).build()
    }
}

interface PlatformPlayer {
    val name: String
}