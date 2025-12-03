/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.util.db

import com.google.gson.Gson
import com.zaxxer.hikari.HikariDataSource
import java.util.UUID
import kotlin.io.use

/**
 * ユーザーデータを管理するデータベースクラス
 *
 * @property dataSource HikariCP データソース
 * @since 0.0.2
 */
class UserDB(
    private val dataSource: HikariDataSource
) {

    /**
     * テーブルを作成
     *
     * uuid: プレイヤーUUID (PRIMARY)
     * current_channel: 現在所属チャンネル
     * channel_list: 所属チャンネル一覧（カンマ区切り）
     */
    init {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS users (
                    uuid VARCHAR(36) PRIMARY KEY,
                    current_channel VARCHAR(64),
                    channel_list TEXT
                );
                """
            ).execute()
        }
    }

    /**
     * ユーザー情報を作成する
     *
     * @param uuid プレイヤーUUID
     * @param currentChannel 現在所属チャンネル
     * @param channels 所属チャンネル一覧
     */
    fun insertUser(uuid: UUID, currentChannel: String?, channels: List<String>) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
            MERGE INTO users (uuid, current_channel, channel_list)
            KEY(uuid)
            VALUES (?, ?, ?)
            """.trimIndent()
            ).use { ps ->
                ps.setString(1, uuid.toString())
                ps.setString(2, currentChannel)
                ps.setString(3, Gson().toJson(channels))
                ps.executeUpdate()
            }
        }
    }

    /**
     * ユーザーが存在するか確認する
     *
     * @param uuid プレイヤーUUID
     * @return 存在すれば true
     */
    fun exists(uuid: UUID): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE uuid = ?"
            ).apply {
                setString(1, uuid.toString())
            }.executeQuery().use { rs ->
                rs.next()
                return rs.getInt(1) > 0
            }
        }
    }

    /**
     * ユーザーが存在しない場合に作成する
     *
     * @param uuid プレイヤーUUID
     */
    fun createIfNotExists(uuid: UUID) {
        if (!exists(uuid)) {
            insertUser(uuid, null, emptyList())
        }
    }

    /**
     * 現在チャンネルを取得
     *
     * @param uuid プレイヤーUUID
     * @return 現在チャンネル または null
     */
    fun getCurrentChannel(uuid: UUID): String? =
        getUser(uuid)?.first

    /**
     * 所属チャンネル一覧を取得
     *
     * @param uuid プレイヤーUUID
     * @return 所属チャンネル一覧
     */
    fun getChannelList(uuid: UUID): List<String> =
        getUser(uuid)?.second ?: emptyList()

    /**
     * ユーザーの現在チャンネルを更新する
     *
     * @param uuid プレイヤーUUID
     * @param current チャンネル名
     */
    fun updateCurrentChannel(uuid: UUID, current: String?) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE users
                SET current_channel = ?
                WHERE uuid = ?;
                """
            ).apply {
                setString(1, current)
                setString(2, uuid.toString())
            }.executeUpdate()
        }
    }

    /**
     * チャンネルリストを更新する
     *
     * @param uuid プレイヤーUUID
     * @param list 所属チャンネル一覧
     */
    fun updateChannelList(uuid: UUID, list: List<String>) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE users
                SET channel_list = ?
                WHERE uuid = ?;
                """
            ).apply {
                setString(1, list.joinToString(","))
                setString(2, uuid.toString())
            }.executeUpdate()
        }
    }

    /**
     * ユーザーの所属チャンネルに追加する
     *
     * @param uuid プレイヤーUUID
     * @param channel 追加するチャンネル名
     */
    fun addChannel(uuid: UUID, channel: String) {
        val current = getUser(uuid)
        val list = current?.second?.toMutableList() ?: mutableListOf()

        if (!list.contains(channel)) {
            list.add(channel)
            updateChannelList(uuid, list)
        }
    }

    /**
     * ユーザーの所属チャンネルから指定のチャンネルを削除する
     *
     * @param uuid プレイヤーUUID
     * @param channel 削除するチャンネル名
     */
    fun removeChannel(uuid: UUID, channel: String) {
        val current = getUser(uuid)
        val list = current?.second?.toMutableList() ?: mutableListOf()

        if (list.contains(channel)) {
            list.remove(channel)
            updateChannelList(uuid, list)
        }
    }

    /**
     * UUID のユーザーデータを取得
     *
     * @param uuid プレイヤーUUID
     * @return Triple<現在チャンネル, 所属チャンネル一覧, UUID> または null
     */
    fun getUser(uuid: UUID): Triple<String?, List<String>, UUID>? {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT * FROM users WHERE uuid = ?"
            ).apply {
                setString(1, uuid.toString())
            }.executeQuery().use { rs ->
                return if (rs.next()) {
                    Triple(
                        rs.getString("current_channel"),
                        rs.getString("channel_list")?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
                        uuid
                    )
                } else null
            }
        }
    }

    /**
     * 指定したUUIDのユーザーの現在チャンネルとチャンネルリストをまとめて更新する
     *
     * @param uuid プレイヤーUUID
     * @param currentChannel 現在所属チャンネル
     * @param channels 所属チャンネル一覧
     */
    fun updateUser(uuid: UUID, currentChannel: String?, channels: List<String>) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE users
                SET current_channel = ?, channel_list = ?
                WHERE uuid = ?;
                """
            ).apply {
                setString(1, currentChannel)
                setString(2, channels.joinToString(","))
                setString(3, uuid.toString())
            }.executeUpdate()
        }
    }

    /**
     * UUID のユーザーデータ削除
     *
     * @param uuid プレイヤーUUID
     */
    fun deleteUser(uuid: UUID) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "DELETE FROM users WHERE uuid = ?"
            ).apply {
                setString(1, uuid.toString())
            }.executeUpdate()
        }
    }

    /**
     * UUID のユーザーデータを取得。存在しない場合は作成してから取得する
     *
     * @param uuid プレイヤーUUID
     * @return Triple<現在チャンネル, 所属チャンネル一覧, UUID>
     */
    fun getOrCreateUser(uuid: UUID): Triple<String?, List<String>, UUID> {
        return getUser(uuid) ?: run {
            createIfNotExists(uuid)
            getUser(uuid)!!
        }
    }

    /**
     * 全ユーザーのデータを取得
     *
     * @return List<Triple<現在チャンネル, 所属チャンネル一覧, UUID>>
     */
    fun getAllUsers(): List<Triple<String?, List<String>, UUID>> {
        val result = mutableListOf<Triple<String?, List<String>, UUID>>()
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                "SELECT uuid, current_channel, channel_list FROM users"
            ).use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        val uuidStr = rs.getString("uuid")
                        val currentChannel = rs.getString("current_channel")
                        val channelList = rs.getString("channel_list")
                        val channelListParsed = channelList
                            ?.split(",")
                            ?.filter { it.isNotBlank() }
                            ?: emptyList()
                        val uuid = UUID.fromString(uuidStr)
                        result.add(Triple(currentChannel, channelListParsed, uuid))
                    }
                }
            }
        }
        return result
    }

    /**
     * データソースを安全にクローズ
     */
    fun close() {
        try {
            dataSource.close()
        } catch (e: Exception) {
            System.err.println("Warning: Failed to close dataSource: ${e.message}")
        }
    }
}