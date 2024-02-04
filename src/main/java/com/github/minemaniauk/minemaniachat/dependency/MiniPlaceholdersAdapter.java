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

package com.github.minemaniauk.minemaniachat.dependency;

import com.velocitypowered.api.proxy.Player;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;

public class MiniPlaceholdersAdapter {

    /**
     * Parse the mini placeholders.
     *
     * @param message The instance of the message to parse.
     * @param player  The instance of the player.
     * @return The parsed message.
     */
    public static Component parseMiniPlaceholders(String message, @Nullable Player player) {
        if (player != null) {
            return MiniMessage.miniMessage().deserialize(
                    message,
                    MiniPlaceholders.getAudienceGlobalPlaceholders(Audience.audience(player))
            );
        }

        return MiniMessage.miniMessage().deserialize(
                message,
                MiniPlaceholders.getGlobalPlaceholders()
        );
    }
}
