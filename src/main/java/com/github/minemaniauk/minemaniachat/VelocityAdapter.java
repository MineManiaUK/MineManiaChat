/*
 * MineManiaAPI
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

import com.github.minemaniauk.api.user.MineManiaUser;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VelocityAdapter {

    /**
     * Used to get the instance of a player from a mine mania user.
     *
     * @param user The instance of the user.
     * @return The optional player instance.
     * It will be empty if the player is not online on this server.
     */
    public static Optional<Player> getPlayer(@NotNull MineManiaUser user) {
        return MineManiaChat.getInstance().getProxyServer().getPlayer(user.getUniqueId());
    }

    /**
     * Used to get the instance of a mine mania user from a player.
     *
     * @param player The instance of the player.
     * @return The requested mine mania user instance.
     */
    public static @NotNull MineManiaUser getUser(@NotNull Player player) {
        return new MineManiaUser(player.getUniqueId(), player.getUsername());
    }
}
