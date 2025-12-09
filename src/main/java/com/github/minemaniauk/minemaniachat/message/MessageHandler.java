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

package com.github.minemaniauk.minemaniachat.message;

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.github.minemaniauk.minemaniachat.User;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;


public class MessageHandler {

    public void sendPlayerMessage(Player from, Player to, String message){

        if (!from.hasPermission("chat.bypass.filter.banned-words")) {
            if (MineManiaChat.getInstance().getChatHandler().containsBannedWords(message)) {
                new User(from).sendMessage("&c&l> &7Please do not use &cbanned words &7in your message.");
                MineManiaChat.getInstance().getChatHandler().notifyStaff(from, "Sent a private message to " + to.getUsername() + " with Banned words!", message);
                return;
            }
        }

        if (!from.hasPermission("chat.bypass.filter.url")) {
            if (MineManiaChat.getInstance().getChatHandler().URL_PATTERN.matcher(message).find()) {
                new User(from).sendMessage("&c&l> &7Please do not use &cURLs &7in your message.");
                MineManiaChat.getInstance().getChatHandler().notifyStaff(from, "Sent a private message to " + to.getUsername() + " with a URL!", message);
                return;
            }
        }

        if (!from.hasPermission("chat.bypass.private-message.disablement")) {
            if (!MineManiaChat.getInstance().getDataManager().CanPlayerPm(from)) {
                new User(from).sendMessage("&cYou are not allowed to use private messaging");
                MineManiaChat.getInstance().getChatHandler().notifyStaff(from, "Sent a private message to " + to.getUsername() + " but has private messaging disabled", message);
                return;
            }
        }

        MineManiaChat.getInstance().getLogger().info(from.getUsername() + " -> " + to.getUsername() + ": " + message);

        for (Player player : MineManiaChat.getInstance().getProxyServer().getAllPlayers()){
            if (player.hasPermission("chat.private-message.spy") && MineManiaChat.getInstance().getDataManager().isSpying(player)){
                new User(player).sendMessage("&8&o" + from.getUsername() + " -> " + to.getUsername() + " : &o" + message);
            }
        }

        from.sendMessage(
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        "&f✉ &7&ome -> &f&o" + to.getUsername() + "&7&o: " + message
                )
        );

        to.sendMessage(
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        "&f✉ &f&o" + from.getUsername() + " &7-> &7&ome &7&o: " + message
                )
        );
    }

    public void sendConsoleMessage(Player to, String message) {

        for (Player player : MineManiaChat.getInstance().getProxyServer().getAllPlayers()) {
            if (player.hasPermission("chat.private-message.spy") && MineManiaChat.getInstance().getDataManager().isSpying(player)) {
                new User(player).sendMessage("&8" + "CONSOLE" + " -> " + to.getUsername() + " : &o" + message);
            }
        }

        MineManiaChat.getInstance().getLogger().info(LegacyComponentSerializer.legacyAmpersand().deserialize(
                "&f✉ &7&ome -> &f&o" + to.getUsername() + "&7&o: " + message
        ));

        to.sendMessage(
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        "&f✉ &f&o" + "CONSOLE" + " &7-> &7&ome &7&o: " + message
                )
        );
    }
}
