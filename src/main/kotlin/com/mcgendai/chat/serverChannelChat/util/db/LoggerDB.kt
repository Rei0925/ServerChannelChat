/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.serverChannelChat.util.db

import com.mcgendai.chat.serverChannelChat.channel.LogEntry
import com.zaxxer.hikari.HikariDataSource
import java.util.UUID

/**
 * データベースのLogシステムを管理するクラス
 *
 * @property dataSource
 * @since 0.0.2
 */
class LoggerDB(
    private val dataSource: HikariDataSource
) {
    /**
     * テーブルを作成
     */
    init {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS log (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    channel_name VARCHAR(64) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    time BIGINT NOT NULL,
                    content TEXT NOT NULL,
                    ngword BOOLEAN NOT NULL
                );
                """
            ).execute()
        }
    }

    /**
     * ログを挿入する関数
     *
     * @param channelName チャンネル名
     * @param playerUuid プレイヤーのUUID
     * @param time Longの時刻
     * @param content メッセージ内容
     * @param ngWord NGワードが含まれているかどうか
     */
    fun insertLog(
        channelName: String,
        playerUuid: UUID,
        time: Long,
        content: String,
        ngWord: Boolean
    ) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO log (channel_name, player_uuid, time, content, ngword)
                VALUES (?, ?, ?, ?, ?);
                """
            ).apply {
                setString(1, channelName)
                setString(2, playerUuid.toString())
                setLong(3, time)
                setString(4, content)
                setBoolean(5, ngWord)
            }.executeUpdate()
        }
    }

    /**
     * ログをIDで削除する関数
     *
     * @param id LogID
     */
    fun deleteLog(id: Int) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "DELETE FROM log WHERE id = ?"
            ).apply {
                setInt(1, id)
            }.executeUpdate()
        }
    }

    /**
     * チャンネル名でログを検索する関数
     *
     * @param channelName チャンネル名
     * @return LogEntryデータクラスのリスト
     */
    fun searchByChannel(channelName: String): List<LogEntry> {
        val result = mutableListOf<LogEntry>()
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT * FROM log WHERE channel_name = ? ORDER BY time DESC"
            ).apply {
                setString(1, channelName)
            }.executeQuery().use { rs ->
                while (rs.next()) {
                    result.add(
                        LogEntry(
                            id = rs.getInt("id"),
                            channelName = rs.getString("channel_name"),
                            playerUuid = UUID.fromString(rs.getString("player_uuid")),
                            time = rs.getLong("time"),
                            content = rs.getString("content"),
                            ngWord = rs.getBoolean("ngword")
                        )
                    )
                }
            }
        }
        return result
    }

    /**
     * Closes the HikariDataSource safely.
     */
    fun close() {
        try {
            dataSource.close()
        } catch (e: Exception) {
            System.err.println("Warning: Failed to close dataSource: ${e.message}")
        }
    }
}