/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.serverChannelChat

import com.mcgendai.chat.serverChannelChat.paper.ChCommand
import com.mcgendai.chat.serverChannelChat.paper.ChController
import com.mcgendai.chat.serverChannelChat.paper.ChatListener
import com.mcgendai.chat.serverChannelChat.util.CheckNG
import com.mcgendai.chat.serverChannelChat.util.GoogleIME
import com.mcgendai.chat.serverChannelChat.util.Japaniser
import com.mcgendai.chat.serverChannelChat.util.db.LoggerDB
import com.mcgendai.chat.serverChannelChat.util.db.NGwordDB
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.plugin.java.JavaPlugin

/**
 * メインクラス
 *
 * @author Rei0925
 * @version 0.0.1
 * @since 0.0.1
 */
class ServerChannelChatPaper : JavaPlugin() {
    //lateinit var sudachiIME: SudachiIME
    lateinit var japaniser: Japaniser
    lateinit var googleIME: GoogleIME
    lateinit var loggerDB: LoggerDB
    lateinit var nGwordDB: NGwordDB
    lateinit var checkNG: CheckNG
    lateinit var chController: ChController
    override fun onLoad() {
        saveDefaultConfig()
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
        val logEnabled = this.config.getBoolean("channel.log", false)
        val setting = Setting(
            logEnabled
        )

        logger.info("Hello world!")
        //sudachiIME = SudachiIME()
        //japaniser = Japaniser(sudachiIME)
        loggerDB = LoggerDB(dataSource)
        nGwordDB = NGwordDB(dataSource)
        googleIME = GoogleIME()
        japaniser = Japaniser(googleIME)
        checkNG = CheckNG(nGwordDB)
        chController = ChController(nGwordDB)
        server.pluginManager.registerEvents(ChatListener(this,japaniser,setting,loggerDB,checkNG),this)
        ChCommand(chController).register(this)
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
        logger.info("Thank you. Good bye.")
    }
}
