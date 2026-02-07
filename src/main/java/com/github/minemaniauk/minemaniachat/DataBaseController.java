/*
 * MineManiaChat
 * Used for interacting with the database and message broker.
 *
 * Copyright (C) 2023  MineManiaUK Staff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.minemaniauk.minemaniachat;

import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.github.smuddgge.squishyconfiguration.interfaces.ConfigurationSection;
import com.velocitypowered.api.proxy.Player;

import java.sql.*;
import java.util.UUID;

public class DataBaseController {

    private Connection lbConn;
    private Connection pvConn;

    public DataBaseController(Configuration config) {

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            MineManiaChat.getInstance().getLogger().info("MariaDB driver found.");
        } catch (ClassNotFoundException e) {
            MineManiaChat.getInstance().getLogger().error("MariaDB driver NOT found", e);
        }

        try {
            ConfigurationSection lbSection = config.getSection("database.litebans");
            ConfigurationSection pvSection = config.getSection("database.premium-vanish");

            String lbUrl = lbSection.getString("url");
            String lbUser = lbSection.getString("user");;
            String lbPassword = lbSection.getString("password");;

            String pvUrl = pvSection.getString("url");
            String pvUser = pvSection.getString("user");;
            String pvPassword = pvSection.getString("password");;

            lbConn = DriverManager.getConnection(lbUrl, lbUser, lbPassword);
            pvConn = DriverManager.getConnection(pvUrl, pvUser, pvPassword);
        }
        catch(SQLException e) {
            MineManiaChat.getInstance().getLogger().atError().setCause(e)
                    .log("Could not connect to db");
        }

    }

    public boolean isPlayerMuted(Player player) {
        try {
            UUID uuid = player.getUniqueId();

            String sql =
                    "SELECT EXISTS (" +
                            "  SELECT 1 " +
                            "  FROM litebans_mutes " +
                            "  WHERE uuid = ? " +
                            "    AND active = b'1' " +
                            "    AND (until = 0 OR until > UNIX_TIMESTAMP())" +
                            ")";

            try (PreparedStatement ps = lbConn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean(1);
                    }
                }
            }

            return false;
        }
        catch (SQLException e) {
            MineManiaChat.getInstance().getLogger().atError().setCause(e).log("A Database error occurred while checking if a player was muted");
            return false;
        }

    }

    public boolean isPlayerVanished(Player player) {
        try {

            UUID uuid = player.getUniqueId();

            String sql =
                    "SELECT Vanished = 1 " +
                            "FROM premiumvanish_playerdata " +
                            "WHERE UUID = ?";

            try (PreparedStatement ps =pvConn.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBoolean(1);
                    }
                }
            }

            return false;
        }
        catch (SQLException e) {
            MineManiaChat.getInstance().getLogger().atError().setCause(e).log("A Database error occurred while checking if a player was vanished");
            return false;
        }
    }

    public void close() {
        closeQuietly(lbConn);
        closeQuietly(pvConn);
    }

    private void closeQuietly(Connection c) {
        if (c == null) return;
        try { c.close(); } catch (SQLException ignored) {}
    }
}