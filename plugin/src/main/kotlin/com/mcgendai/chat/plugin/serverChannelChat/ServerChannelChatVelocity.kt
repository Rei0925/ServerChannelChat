/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.plugin.serverChannelChat

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
import com.mcgendai.chat.plugin.serverChannelChat.velocity.ChCommand
import com.mcgendai.chat.plugin.serverChannelChat.velocity.ChatListener
import com.mcgendai.chat.plugin.serverChannelChat.velocity.TellCommand
import com.mcgendai.chat.plugin.serverChannelChat.velocity.VelocityServer

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.Logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import javax.inject.Inject

/**
 * メインクラス
 *
 * @platform Velocity
 * @author Rei0925
 * @version 0.0.3
 * @since 0.0.3
 */
@Plugin(id = "serverchannelchat", name = "ServerChannelChat", version = BuildVersion.VERSION, authors = ["Rei0925"], url = "https://chat.mcgendai.com")
class ServerChannelChatVelocity @Inject constructor(
    private val proxyServer: ProxyServer,
    @DataDirectory private val dataDirectory: Path,
    private val logger: Logger,
) {
    lateinit var dataSource: HikariDataSource
    lateinit var setting: Setting

    fun initialize() {
        val dataFolder: File = dataDirectory.toFile() // ← Path を File に変換
        if (!dataFolder.exists()) dataFolder.mkdirs()

        val configFile = File(dataFolder, "config.yml")
        if (!configFile.exists()) {
            this.javaClass.classLoader.getResourceAsStream("config.yml")?.use { input ->
                Files.copy(input, configFile.toPath())
            }
        }

        val config = org.yaml.snakeyaml.Yaml().load<Map<String, Any>>(configFile.inputStream())
        val logEnabled = config["channel.log"] as? Boolean ?: false
        setting = Setting(logEnabled)

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:h2:${dataFolder.absolutePath}/chatdb;MODE=MySQL"
            driverClassName = "org.h2.Driver"
            maximumPoolSize = 10
            username = "sa"
            password = ""
        }

        dataSource = HikariDataSource(hikariConfig)
    }

    lateinit var japaniser: Japaniser
    lateinit var googleIME: GoogleIME
    lateinit var loggerDB: LoggerDB
    lateinit var nGwordDB: NGwordDB
    lateinit var channelDB: ChannelDB
    lateinit var userDB: UserDB
    lateinit var checkNG: CheckNG
    lateinit var chController: ChController
    lateinit var tellController: TellController

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        initialize()
        logger.info("Velocityで起動しました。")
        googleIME = GoogleIME()
        japaniser = Japaniser(googleIME)
        loggerDB = LoggerDB(dataSource)
        nGwordDB = NGwordDB(dataSource)
        channelDB = ChannelDB(dataSource)
        userDB = UserDB(dataSource)
        checkNG = CheckNG(nGwordDB)
        chController = ChController(nGwordDB,channelDB,userDB)
        tellController = TellController(VelocityServer(proxyServer),japaniser)
        // コマンド登録
        TellCommand(tellController,proxyServer).register()
        ChCommand(chController,proxyServer).register()
        proxyServer.eventManager.register(this, ChatListener(proxyServer,this,japaniser,setting,loggerDB,checkNG))

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
}