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

package com.github.minemaniauk.minemaniachat.discord.link;

import com.github.minemaniauk.minemaniachat.MineManiaChat;

import java.util.UUID;

public class LinkStorage {

    public void saveLink(UUID minecraftUuid, String username, String discordUserId) {
        MineManiaChat.getInstance()
                .getLinksConfig()
                .set("links.minecraft-to-discord." + minecraftUuid, discordUserId);

        MineManiaChat.getInstance()
                .getLinksConfig()
                .set("links.discord-to-minecraft." + discordUserId, minecraftUuid.toString());

        MineManiaChat.getInstance()
                .getLinksConfig()
                .set("links.minecraft-names." + minecraftUuid, username);

        MineManiaChat.getInstance()
                .getLinksConfig()
                .save();
    }

    public void removeLink(UUID minecraftUuid, String discordUserId) {
        MineManiaChat.getInstance()
                .getLinksConfig()
                .set("links.minecraft-to-discord." + minecraftUuid.toString(), null);

        MineManiaChat.getInstance()
                .getLinksConfig()
                .set("links.discord-to-minecraft." + discordUserId, null);

        MineManiaChat.getInstance()
                .getLinksConfig()
                .save();
    }

    public void updateMinecraftUsername(UUID minecraftUuid, String username) {
        String path = "links.minecraft-names." + minecraftUuid.toString();

        String currentUsername = MineManiaChat.getInstance()
                .getLinksConfig()
                .getString(path);

        if (username.equalsIgnoreCase(currentUsername)) {
            return;
        }

        MineManiaChat.getInstance()
                .getLinksConfig()
                .set(path, username);

        MineManiaChat.getInstance()
                .getLinksConfig()
                .save();
    }

    public String getDiscordId(UUID minecraftUuid) {
        return MineManiaChat.getInstance()
                .getLinksConfig()
                .getString("links.minecraft-to-discord." + minecraftUuid.toString());
    }

    public UUID getMinecraftUuid(String discordUserId) {
        String uuidString = MineManiaChat.getInstance()
                .getLinksConfig()
                .getString("links.discord-to-minecraft." + discordUserId);

        if (uuidString == null) {
            return null;
        }

        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isMinecraftLinked(UUID minecraftUuid) {
        return getDiscordId(minecraftUuid) != null;
    }

    public boolean isDiscordLinked(String discordUserId) {
        return getMinecraftUuid(discordUserId) != null;
    }
}