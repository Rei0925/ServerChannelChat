/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.util.db

import com.mcgendai.chat.common.serverChannelChat.channel.NGEntry
import com.zaxxer.hikari.HikariDataSource

/**
 * NGワールド管理するクラス
 *
 * @property dataSource
 * @since 0.0.2
 */
class NGwordDB(
    private val dataSource: HikariDataSource
) {
    /**
     * テーブルを作成
     */
    init {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS ngword (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    word VARCHAR(128) NOT NULL,
                    severity INT NOT NULL,
                    usage_count INT NOT NULL DEFAULT 0
                );
                """
            ).execute()
        }
    }

    /**
     * NGワードを挿入する関数
     *
     * @param value NGワード
     * @param severity 重大度 1~4
     */
    fun insert(value: String, severity: Int) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                INSERT INTO ngword (word, severity, usage_count)
                VALUES (?, ?, 0);
                """
            ).apply {
                setString(1, value)
                setInt(2, severity)
            }.executeUpdate()
        }
    }

    /**
     * NGワードを削除する関数
     *
     * @param id NGワードID
     */
    fun delete(id: Int) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "DELETE FROM ngword WHERE id = ?"
            ).apply {
                setInt(1, id)
            }.executeUpdate()
        }
    }

    /**
     * NGワードの使用回数を追加する関数
     *
     * @param value NGワード
     */
    fun incrementUsage(value: String) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE ngword
                SET usage_count = usage_count + 1
                WHERE word = ?;
                """
            ).apply {
                setString(1, value)
            }.executeUpdate()
        }
    }

    /**
     * NGワードを検索する関数
     *
     * @param keyword NGワード
     * @return NGEntryのリスト
     */
    fun search(keyword: String): List<NGEntry> {
        val result = mutableListOf<NGEntry>()
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT * FROM ngword WHERE word LIKE ?"
            ).apply {
                setString(1, "%$keyword%")
            }.executeQuery().use { rs ->
                while (rs.next()) {
                    result.add(
                        NGEntry(
                            id = rs.getInt("id"),
                            value = rs.getString("word"),
                            severity = rs.getInt("severity"),
                            usageCount = rs.getInt("usage_count")
                        )
                    )
                }
            }
        }
        return result
    }

    /**
     * 全てのNGワードの値を取得する関数
     *
     * @return NGワードの値のリスト
     */
    fun getNGWords(): List<String> {
        val words = mutableListOf<String>()
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT word FROM ngword"
            ).executeQuery().use { rs ->
                while (rs.next()) {
                    words.add(rs.getString("word"))
                }
            }
        }
        return words
    }

    /**
     * 全てのNGワードを取得する関数
     *
     * @return NGEntryのリスト
     */
    fun getAll(): List<NGEntry> {
        val result = mutableListOf<NGEntry>()
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT * FROM ngword"
            ).executeQuery().use { rs ->
                while (rs.next()) {
                    result.add(
                        NGEntry(
                            id = rs.getInt("id"),
                            value = rs.getString("word"),
                            severity = rs.getInt("severity"),
                            usageCount = rs.getInt("usage_count")
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