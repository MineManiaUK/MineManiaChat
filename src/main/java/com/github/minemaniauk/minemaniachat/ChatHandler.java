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
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Used to handle chat events.
 * When the post-chat event is called it will
 * handle the formatting and filtering.
 */
public class ChatHandler {

    private final @NotNull Configuration configuration;
    private final @NotNull Configuration bannedWords;
    public final Pattern URL_PATTERN = Pattern.compile(
            "(https?://[^\\s]+)|(www\\.[^\\s]+)|([a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"
    );

    /**
     * Used to create a new instance of the chat handler.
     *
     * @param configuration The instance of the configuration
     *                      to format and filter the chat.
     */
    public ChatHandler(@NotNull Configuration configuration, @NotNull Configuration bannedWords) {
        this.configuration = configuration;
        this.bannedWords = bannedWords;
    }

    @Subscribe
    public void onEvent(@NotNull PlayerChatEvent event) {
            Player sendingPlayer = event.getPlayer();

            event.setResult(PlayerChatEvent.ChatResult.denied());

            if (event.getMessage().startsWith("[event cancelled]")){
                return;
            }

            // Check for URLs
            if (!sendingPlayer.hasPermission("chat.bypass.filter.url")) {
                if (URL_PATTERN.matcher(event.getMessage()).find()) {
                    new User(sendingPlayer).sendMessage("&c&l> &7Please do not use &cURLs &7in your message.");
                    notifyStaff(sendingPlayer, "Sent Message with a URL!", event.getMessage());
                    return;
                }
            }

            // Check for banned words.
            if (!sendingPlayer.hasPermission("chat.bypass.filter.banned-words")) {
                if (this.containsBannedWords(event.getMessage())) {
                    new User(sendingPlayer).sendMessage("&c&l> &7Please do not use &cbanned words &7in your message.");
                    notifyStaff(sendingPlayer, "Sent Message with Banned words!", event.getMessage());
                    return;
                }
            }

            for (Player p : MineManiaChat.getInstance().getProxyServer().getAllPlayers()) {
                p.sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                                this.formatMessage(event.getMessage(), sendingPlayer)
                        )
                );
            }
    }

    /**
     * Used to append chat formatting to the message.
     *
     * @param message The instance of the chat event.
     * @param player The instance of the player.
     */
    public String formatMessage(@NotNull String message, @NotNull Player player) {
        ConfigurationSection formatSection = this.configuration.getSection("format");

        String prefix = "";
        String postfix = "";

        // Loop though chat formatting.
        for (String key : formatSection.getKeys()) {
            String permission = "chat." + key;
            if (!player.hasPermission(permission)) continue;
            prefix = formatSection.getSection(key).getString("prefix", "");
            postfix = formatSection.getSection(key).getString("postfix", "");
            break;
        }
        return prefix + " &f" + player.getUsername() + " &7: &f" + message + " &f" + postfix;
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
        List<String> bannedPhrases = this.bannedWords.getListString("banned-words", new ArrayList<>());

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

    public void notifyStaff(Player player, String staffMessage ,String message){
        Collection<Player> allPlayers = MineManiaChat.getInstance().getProxyServer().getAllPlayers();

        for (Player p : allPlayers){
            if (p.hasPermission("chat.notify")){
                User u = new User(p);
                u.sendMessage("&cPlayer " + player.getUsername() + " " + staffMessage + "\n" + message);
            }
        }
    }
}
