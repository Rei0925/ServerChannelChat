/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat

import co.aikar.commands.BungeeCommandManager

import com.mcgendai.chat.common.serverChannelChat.channel.ChController
import com.mcgendai.chat.common.serverChannelChat.channel.ChannelData
import com.mcgendai.chat.common.serverChannelChat.channel.ChatFormat
import com.mcgendai.chat.common.serverChannelChat.channel.TellController
import com.mcgendai.chat.common.serverChannelChat.util.CheckNG
import com.mcgendai.chat.common.serverChannelChat.util.GoogleIME
import com.mcgendai.chat.common.serverChannelChat.util.Japaniser
import com.mcgendai.chat.common.serverChannelChat.util.db.ChannelDB
import com.mcgendai.chat.common.serverChannelChat.util.db.LoggerDB
import com.mcgendai.chat.common.serverChannelChat.util.db.NGwordDB
import com.mcgendai.chat.common.serverChannelChat.util.db.UserDB
import com.mcgendai.chat.plugin.serverChannelChat.bungee.BungeeServer
import com.mcgendai.chat.plugin.serverChannelChat.bungee.ChCommand
import com.mcgendai.chat.plugin.serverChannelChat.bungee.ChatListener
import com.mcgendai.chat.plugin.serverChannelChat.bungee.TellCommand

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.util.UUID

/**
 * メインクラス
 *
 * @platform BungeeCord
 * @author Rei0925
 * @version 0.0.3
 * @since 0.0.3
 */
class ServerChannelChatBungee: Plugin() {
    lateinit var japaniser: Japaniser
    lateinit var googleIME: GoogleIME
    lateinit var loggerDB: LoggerDB
    lateinit var nGwordDB: NGwordDB
    lateinit var channelDB: ChannelDB
    lateinit var userDB: UserDB
    lateinit var checkNG: CheckNG
    lateinit var chController: ChController
    lateinit var tellController: TellController

    override fun onLoad() {
        val dataFolder = dataFolder
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            getResourceAsStream("config.yml").use { input ->
                if (input != null) {
                    Files.copy(input, configFile.toPath())
                }
            }
        }
    }

    override fun onEnable() {
        val dataFolder = dataFolder
        if (!dataFolder.exists()) dataFolder.mkdirs()

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:h2:${dataFolder.absolutePath}/chatdb;MODE=MySQL"
            driverClassName = "org.h2.Driver"
            maximumPoolSize = 10
            username = "sa"
            password = ""
        }

        val dataSource = HikariDataSource(config)
        val configFile = File(dataFolder, "config.yml")
        val yamlConfig: Configuration = if (configFile.exists()) {
            YamlConfiguration.getProvider(YamlConfiguration::class.java).load(configFile)
        } else {
            YamlConfiguration.getProvider(YamlConfiguration::class.java).load(InputStreamReader(getResourceAsStream("config.yml")!!))
        }

        val logEnabled = yamlConfig.getBoolean("channel.log", false)
        val setting = Setting(
            logEnabled
        )

        logger.info("BungeeCordで起動しました。")
        //sudachiIME = SudachiIME()
        //japaniser = Japaniser(sudachiIME)
        loggerDB = LoggerDB(dataSource)
        nGwordDB = NGwordDB(dataSource)
        channelDB = ChannelDB(dataSource)
        userDB = UserDB(dataSource)
        googleIME = GoogleIME()
        japaniser = Japaniser(googleIME)
        checkNG = CheckNG(nGwordDB)
        chController = ChController(nGwordDB, channelDB, userDB)
        tellController = TellController(BungeeServer(),japaniser)
        val commandManager = BungeeCommandManager(this)
        val completions = commandManager.commandCompletions

        // @player を登録（オンラインプレイヤー一覧）
        completions.registerAsyncCompletion("player") { _ ->
            proxy.players.map { it.name }
        }
        commandManager.registerCommand(TellCommand(tellController))
        commandManager.registerCommand(ChCommand(chController))
        proxy.pluginManager.registerListener(this, ChatListener(this,japaniser,setting,loggerDB,checkNG))

        if(channelDB.get("G") == null){
            channelDB.insert(
                ChannelData(
                    name = "G",
                    uuid = UUID.randomUUID(),
                    isGlobalChannel = true,
                    owner = null,
                    member = null,
                    password = null,
                    format = ChatFormat("&f<{player}&f> &f{msg} &f({original}&f) &7@{world} &8{time}"),
                    isNGBypass = false
                )
            )
        }
    }

    override fun onDisable() {
        try {
            loggerDB.close()
        } catch (e: Exception) {
            logger.warning("Failed to close loggerDB: ${e.message}")
        }
        try {
            nGwordDB.close()
        } catch (e: Exception) {
            logger.warning("Failed to close nGwordDB: ${e.message}")
        }
        channelDB.close()
        userDB.close()
        logger.info("Thank you. Good bye.")
    }
}