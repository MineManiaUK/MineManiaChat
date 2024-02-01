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

import com.github.kerbity.kerb.client.listener.EventListener;
import com.github.kerbity.kerb.packet.event.Event;
import com.github.minemaniauk.api.format.ChatFormatPriority;
import com.github.minemaniauk.api.kerb.event.player.PlayerPostChatEvent;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.github.smuddgge.squishyconfiguration.interfaces.ConfigurationSection;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatHandler implements EventListener<PlayerPostChatEvent> {

    private final @NotNull Configuration configuration;

    public ChatHandler(@NotNull Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public @Nullable Event onEvent(PlayerPostChatEvent event) {
        ConfigurationSection formatSection = this.configuration.getSection("format");
        ConfigurationSection channelSection = this.configuration.getSection("channels");
        Optional<Player> optionalPlayer = VelocityAdapter.getPlayer(event.getUser());

        // Check if the player is online.
        if (optionalPlayer.isEmpty()) {
            MineManiaChat.getInstance().getLogger().warn("[ChatEvent] Attempted to get player from user but the player was not online.");
            event.setCancelled(true);
            return event;
        }

        // Get the instance of the player.
        Player player = optionalPlayer.get();

        // Loop though chat formatting.
        for (String key : formatSection.getKeys()) {
            String permission = "chat." + key;
            if (!player.hasPermission(permission)) continue;
            event.getChatFormat()
                    .addPrefix(formatSection.getSection(key).getString("prefix", ""), ChatFormatPriority.HIGH)
                    .addPostfix(formatSection.getSection(key).getString("postfix", ""), ChatFormatPriority.HIGH);
            break;
        }

        for (String string : this.configuration.getListString("banned_words", new ArrayList<>())) {
            if (event.getMessage().toLowerCase().contains(string.toLowerCase())) {
                Optional<Player> optional = VelocityAdapter.getPlayer(event.getUser());
                optional.ifPresent(value -> new User(value).sendMessage("&c&l> &7Please do not include banned words such as &f" + string + "&7 in your message."));
                event.setCancelled(true);
                return event;
            }
        }

        // Loop though channels.
        for (String key : channelSection.getKeys()) {
            List<String> serverList = channelSection.getListString(key);
            if (!serverList.contains(event.getSource().getName())) continue;
            event.addWhitelistedServer(serverList);
        }

        return event;
    }
}
