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
import com.velocitypowered.api.proxy.Player;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LinkManager {

    private final LinkStorage linkStorage;
    private final Map<String, PendingLink> pendingLinks = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    public LinkManager(LinkStorage linkStorage) {
        this.linkStorage = linkStorage;
    }

    public String createLinkCode(Player player) {
        String code;

        do {
            code = String.format("%06d", random.nextInt(1_000_000));
        } while (pendingLinks.containsKey(code));

        pendingLinks.put(code, new PendingLink(player.getUniqueId(), player.getUsername()));
        return code;
    }

    public boolean completeLink(String code, String discordUserId) {
        PendingLink pendingLink = pendingLinks.remove(code);

        if (pendingLink == null || pendingLink.isExpired()) {
            return false;
        }

        UUID minecraftUuid = pendingLink.getMinecraftUuid();
        String minecraftUsername = pendingLink.getMinecraftUsername();

        if (linkStorage.isMinecraftLinked(minecraftUuid)) {
            return false;
        }

        if (linkStorage.isDiscordLinked(discordUserId)) {
            return false;
        }

        linkStorage.saveLink(minecraftUuid, minecraftUsername, discordUserId);
        return true;
    }

    public String getDiscordId(UUID minecraftUuid) { return linkStorage.getDiscordId(minecraftUuid); }

    public UUID getMinecraftUuid(String discordUserId) { return linkStorage.getMinecraftUuid(discordUserId); }

    public String getMinecraftUsername(UUID minecraftUuid) {
        return MineManiaChat.getInstance()
                .getLinksConfig()
                .getString("links.minecraft-names." + minecraftUuid);
    }

    public boolean isMinecraftLinked(UUID minecraftUuid) {
        return linkStorage.isMinecraftLinked(minecraftUuid);
    }

    public boolean isDiscordLinked(String discordUserId) {
        return linkStorage.isDiscordLinked(discordUserId);
    }
}