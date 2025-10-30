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

import com.eduardomcb.discord.webhook.WebhookClient;
import com.eduardomcb.discord.webhook.WebhookManager;
import com.eduardomcb.discord.webhook.models.Embed;
import com.eduardomcb.discord.webhook.models.Field;
import com.github.kerbity.kerb.client.listener.EventListener;
import com.github.kerbity.kerb.packet.event.Event;
import com.github.minemaniauk.api.format.ChatFormatPriority;
import com.github.minemaniauk.api.kerb.event.player.PlayerPostChatEvent;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.github.smuddgge.squishyconfiguration.interfaces.ConfigurationSection;
import com.velocitypowered.api.proxy.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.sql.Time;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * Used to handle chat events.
 * When the post-chat event is called it will
 * handle the formatting and filtering.
 */
public class ChatHandler implements EventListener<PlayerPostChatEvent> {

    private final @NotNull Configuration configuration;

    /**
     * Used to create a new instance of the chat handler.
     *
     * @param configuration The instance of the configuration
     *                      to format and filter the chat.
     */
    public ChatHandler(@NotNull Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public @Nullable Event onEvent(@NotNull PlayerPostChatEvent event) {
            ConfigurationSection channelSection = this.configuration.getSection("channels");
            Optional<Player> optionalPlayer = MineManiaChat.getInstance().getPlayer(event.getUser());

            // Check if the player is online.
            if (optionalPlayer.isEmpty()) {
                MineManiaChat.getInstance().getLogger().warn("[ChatEvent] Attempted to get player from user but the player was not online.");
                event.setCancelled(true);
                return event;
            }

            if (event.getMessage().startsWith("[event cancelled]")){
                event.setCancelled(true);
            }

            // Get the instance of the player.
            Player player = optionalPlayer.get();

            // Format the message.
            this.appendChatFormatting(event, player);

            // Check for banned words.
                if (this.containsBannedWords(event.getMessage())) {
                    new User(player).sendMessage("&c&l> &7Please do not use &cbanned words &7in your message.");
                    notifyStaff(player, event.getMessage());
                    event.setCancelled(true);
                    return event;
                }



            // Loop though channels and append the correct ones.
            for (String key : channelSection.getKeys()) {
                List<String> serverList = channelSection.getListString(key);
                if (!serverList.contains(event.getSource().getName())) continue;
                event.addWhitelistedServer(serverList);
            }

        var plugin = MineManiaChat.getInstance();
        var logger = plugin.getToxicityLogger();

        if (logger != null) {
            plugin.getProxyServer().getScheduler()
                    .buildTask(plugin, () -> {
                        try {
                            logger.log(event.getMessage(), event.getUser().getName());
                        } catch (Exception ex) {
                            plugin.getLogger().warn("Perspective log failed: {}", ex.getMessage());
                        }
                    })
                    .schedule();
        }

        return event;
    }

    /**
     * Used to append chat formatting to the message.
     *
     * @param event  The instance of the chat event.
     * @param player The instance of the player.
     */
    public void appendChatFormatting(@NotNull PlayerPostChatEvent event, @NotNull Player player) {
        ConfigurationSection formatSection = this.configuration.getSection("format");

        // Loop though chat formatting.
        for (String key : formatSection.getKeys()) {
            String permission = "chat." + key;
            if (!player.hasPermission(permission)) continue;
            event.getChatFormat()
                    .addPrefix(formatSection.getSection(key).getString("prefix", ""), ChatFormatPriority.HIGH)
                    .addPostfix(formatSection.getSection(key).getString("postfix", ""), ChatFormatPriority.HIGH);
            break;
        }
    }

    /**
     * Used to check if a message contains banned words.
     *
     * @param message The instance of the message.
     * @return True if it contains bad words.
     */
    public boolean containsBannedWords(@NotNull String message) {
        message = String.join("", Arrays.stream(message.toLowerCase().split(""))
                .filter(character -> character.matches("([a-z]|[A-Z]| )"))
                .toList()
        );
        System.out.println(message);
        List<String> bannedPhrases = this.configuration.getListString("banned_words", new ArrayList<>());

        // Loop though each word in the message.
        for (final String bannedPhrase : bannedPhrases.stream().map(String::toLowerCase).toList()) {

            // Find the position of the phrase in the message.
            int bannedPhraseIndex = message.indexOf(bannedPhrase);

            // Check if the banned phrase does not exist.
            if (bannedPhraseIndex == -1) continue;

            boolean startOfMessage = bannedPhraseIndex == 0;
            boolean endOfMessage = bannedPhraseIndex + bannedPhrase.length() >= message.length();

            // Check if the phrase is the entire message.
            if (message.equals(bannedPhrase)) return true;

            // Check if the message contains the banned phrase
            // and spaces before and after.
            if (message.contains(" " + bannedPhrase + " ")) return true;

            // Check if the phrase is at the start of
            // the message and has a space after.
            if (startOfMessage && message.contains(bannedPhrase + " ")) return true;

            // Check if the phrase is at the end of
            // the message and has a space before.
            if (endOfMessage && message.contains(" " + bannedPhrase)) return true;
        }

        return false;
    }

    public void notifyStaff(Player player ,String message){
        Collection<Player> allPlayers = MineManiaChat.getInstance().getProxyServer().getAllPlayers();

        for (Player p : allPlayers){
            if (p.hasPermission("chat.notify")){
                User u = new User(p);
                u.sendMessage("&cPlayer " + player.getUsername() + " Sent Message with Banned words\n" + message );
            }
        }
    }
}
