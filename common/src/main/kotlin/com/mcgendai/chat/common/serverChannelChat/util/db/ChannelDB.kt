/**
 * @author     Rei0925
 * @license    LGPLv3
 * @copyright  Copyright Rei0925 2025
 */
package com.mcgendai.chat.common.serverChannelChat.util.db

import com.google.gson.Gson
import com.mcgendai.chat.common.serverChannelChat.channel.ChannelData
import com.mcgendai.chat.common.serverChannelChat.channel.ChatFormat
import com.zaxxer.hikari.HikariDataSource
import java.sql.ResultSet
import java.util.*
import kotlin.io.use

/**
 * ChannelDB provides database access for channel data.
 *
 * This class manages the SQLite table for channels, and provides
 * methods to insert, delete, retrieve, and list channels.
 *
 * @since 0.0.3
 */
class ChannelDB(
    private val dataSource: HikariDataSource
) {
    private val gson = Gson()


    init {
        // Create the channel table if it does not exist
        dataSource.connection.use { conn ->
            conn.createStatement().use { st ->
                st.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS channel (
                        name VARCHAR(64) PRIMARY KEY,
                        uuid CHAR(36) NOT NULL,
                        isGlobalChannel BOOLEAN NOT NULL,
                        owner CHAR(36),
                        members TEXT,
                        password VARCHAR(128),
                        format VARCHAR(64),
                        isNGBypass BOOLEAN NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }

    /**
     * Inserts a channel into the database.
     *
     * @param channel the ChannelData object to insert or replace
     * @since 0.0.3
     */
    fun insert(channel: ChannelData) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
            MERGE INTO channel (name, uuid, isGlobalChannel, owner, members, password, format, isNGBypass) 
            KEY(name)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            ).use { ps ->
                ps.setString(1, channel.name)
                ps.setString(2, channel.uuid.toString())
                ps.setBoolean(3, channel.isGlobalChannel)
                ps.setString(4, channel.owner?.toString())
                ps.setString(5, gson.toJson(channel.member))
                ps.setString(6, channel.password)
                ps.setString(7, channel.format.template)
                ps.setBoolean(8, channel.isNGBypass)
                ps.executeUpdate()
            }
        }
    }

    /**
     * Deletes a channel from the database by name.
     *
     * @param name the name of the channel to delete
     * @since 0.0.3
     */
    fun delete(name: String) {
        dataSource.connection.use { conn ->
            conn.prepareStatement("DELETE FROM channel WHERE name = ?").use { ps ->
                ps.setString(1, name)
                ps.executeUpdate()
            }
        }
    }

    /**
     * Retrieves a channel by name.
     *
     * @param name the name of the channel to retrieve
     * @return the ChannelData object if found, or null if not found
     * @since 0.0.3
     */
    fun get(name: String): ChannelData? {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM channel WHERE name = ?").use { ps ->
                ps.setString(1, name)
                ps.executeQuery().use { rs ->
                    return if (rs.next()) toChannelData(rs) else null
                }
            }
        }
    }

    /**
     * Retrieves all channels from the database.
     *
     * @return a list of all ChannelData objects
     * @since 0.0.3
     */
    fun getAll(): List<ChannelData> {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM channel").use { ps ->
                ps.executeQuery().use { rs ->
                    val list = mutableListOf<ChannelData>()
                    while (rs.next()) {
                        list.add(toChannelData(rs))
                    }
                    return list
                }
            }
        }
    }

    /**
     * Closes the data source.
     *
     * @since 0.0.3
     */
    fun close() {
        dataSource.close()
    }

    /**
     * Convert a ResultSet row to ChannelData.
     */
    private fun toChannelData(rs: ResultSet): ChannelData {
        return ChannelData(
            name = rs.getString("name"),
            uuid = UUID.fromString(rs.getString("uuid")),
            isGlobalChannel = rs.getBoolean("isGlobalChannel"),
            owner = rs.getString("owner")?.let { if (it.isNotBlank()) UUID.fromString(it) else null },
            member = gson.fromJson(rs.getString("members"), Array<UUID>::class.java)?.toList() ?: emptyList(),
            password = rs.getString("password"),
            format = ChatFormat(rs.getString("format")),
            isNGBypass = rs.getBoolean("isNGBypass")
        )
    }

    /**
     * Updates an existing channel in the database by name.
     *
     * @param channel the ChannelData object with updated data
     * @since 0.0.3
     */
    fun update(channel: ChannelData) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                UPDATE channel SET
                    uuid = ?,
                    isGlobalChannel = ?,
                    owner = ?,
                    members = ?,
                    password = ?,
                    format = ?,
                    isNGBypass = ?
                WHERE name = ?
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, channel.uuid.toString())
                ps.setBoolean(2, channel.isGlobalChannel)
                ps.setString(3, channel.owner?.toString())
                ps.setString(4, gson.toJson(channel.member))
                ps.setString(5, channel.password)
                ps.setString(6, channel.format.template)
                ps.setBoolean(7, channel.isNGBypass)
                ps.setString(8, channel.name)
                ps.executeUpdate()
            }
        }
    }
}