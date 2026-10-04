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

package com.github.minemaniauk.minemaniachat.breakoutchat;

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class BreakoutChatManager {

    public static List<BreakoutChat> breakoutChats = new ArrayList<>();

    public static void openNewChat(Player creator, Player member, boolean silent, boolean allowMemberLeave) {
        BreakoutChat chat = new BreakoutChat(creator, member, silent, allowMemberLeave);
        breakoutChats.add(chat);
        creator.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Opened new breakout chat &f" + chat.name));
        if (!silent) {
            if (allowMemberLeave) {
                member.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aYou have been moved into a breakout chat.  Use command /breakout leave to return to main chat"));
            }
            else {
                member.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aYou have been moved into a breakout chat."));
            }
        }

        for (Player p : MineManiaChat.getInstance().getProxyServer().getAllPlayers()) {
            if (p.hasPermission("chat.breakout.notify")) {
                p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7" + creator.getUsername() + " Opened breakout chat use command &f/breakout join " + chat.name + " To join the chat"));
            }
        }
    }

    public static void closeBreakoutChat(BreakoutChat chat) {
        List<Player> players = chat.members;
        for (Player p : players) {
            if (chat.silent) {
                if (p.hasPermission("chat.breakout.notify")) {
                    p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aThe breakout chat you were in has been closed"));
                }
            }
            else {
                p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aThe breakout chat you were in has been closed"));
            }
        }
        chat.members.clear();
        breakoutChats.remove(chat);
    }

    public static void addToBreakoutChat(Player player, BreakoutChat chat) {
        if (chat.members.contains(player)) return;

        BreakoutChat currentBreakoutChat = getBreakoutChat(player);
        if (currentBreakoutChat != null) {
            chat.members.remove(player);
        }

        chat.members.add(player);
        if (!chat.silent) {
            if (chat.allowMemberLeave) {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aYou have been moved into a breakout chat.  Use command /breakout leave to return to main chat"));
            }
            else {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aYou have been moved into a breakout chat."));
            }
        }
    }

    public static void removeFromBreakoutChat(Player player) {
        BreakoutChat chat = getBreakoutChat(player);

        if (chat == null) return;

        chat.members.remove(player);
        if (!chat.silent) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aYou have been moved to the main chat."));
        }
        if (chat.members.isEmpty()) {
            closeBreakoutChat(chat);
        }
    }

    public static void joinBreakoutChat(Player player, BreakoutChat chat) {
        BreakoutChat currentChat = getBreakoutChat(player);
        if (currentChat != null) {
            leaveBreakoutChat(player);
        }
        chat.members.add(player);
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7You have &ajoined &7breakout chat &f" + chat.name));
    }

    public static void leaveBreakoutChat(Player player) {
        BreakoutChat chat = getBreakoutChat(player);

        if (chat == null) return;

        if (!chat.allowMemberLeave)
            if (!player.hasPermission("chat.breakout.leave"))
                return;

        chat.members.remove(player);
        if (chat.members.isEmpty()) {
            closeBreakoutChat(chat);
        }
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7You have &cleft &7breakout chat &f" + chat.name));
    }

    public static void enableBreakoutChat(BreakoutChat chat) {
        chat.enabled = true;
        for (Player p : chat.members) {
            p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7The breakout chat you are in has been &are-enabled"));
        }
    }

    public static void disableBreakoutChat(BreakoutChat chat) {
        chat.enabled = false;
        for (Player p : chat.members) {
            p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7The breakout chat you are in has been &cpaused"));
        }
    }

    public static BreakoutChat getBreakoutChat(String name) {
        return breakoutChats.stream()
                .filter(c -> c.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    public static BreakoutChat getBreakoutChat(Player player) {
        for (BreakoutChat chat : breakoutChats) {
            if (chat.members.contains(player)) {
                return chat;
            }
        }

        return null;
    }
}

