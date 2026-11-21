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
import java.util.concurrent.CopyOnWriteArrayList;

public class BreakoutChatManager {

    public static final List<BreakoutChat> breakoutChats = new CopyOnWriteArrayList<>();

    public static synchronized void openNewChat(Player creator, Player member, boolean silent, boolean locked) {
        BreakoutChat chat = new BreakoutChat(creator, member, silent, locked);
        String baseName = chat.name;
        int suffix = 2;
        while (getBreakoutChat(chat.name) != null) {
            chat.name = baseName + "-" + suffix++;
        }
        detachPlayer(creator);
        detachPlayer(member);
        breakoutChats.add(chat);
        creator.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Opened new breakout chat &f" + chat.name));
        if (!silent) {
            if (!locked) {
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

    public static synchronized void closeBreakoutChat(BreakoutChat chat) {
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

    public static synchronized void addToBreakoutChat(Player player, BreakoutChat chat) {
        if (chat.members.contains(player)) return;

        detachPlayer(player);

        chat.members.add(player);
        if (!chat.silent) {
            if (!chat.locked) {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aYou have been moved into a breakout chat.  Use command /breakout leave to return to main chat"));
            }
            else {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aYou have been moved into a breakout chat."));
            }
        }
    }

    public static synchronized void removeFromBreakoutChat(Player player) {
        BreakoutChat chat = getBreakoutChat(player);

        if (chat == null) return;

        detachPlayer(player);
        if (!chat.silent) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&a&l> &aYou have been moved to the main chat."));
        }
    }

    public static synchronized void joinBreakoutChat(Player player, BreakoutChat chat) {
        BreakoutChat currentChat = getBreakoutChat(player);
        if (currentChat == chat) return;
        if (currentChat != null && currentChat.locked && !player.hasPermission("chat.breakout.leave")) {
            player.sendPlainMessage("You cannot leave your locked breakout chat.");
            return;
        }
        detachPlayer(player);
        chat.members.add(player);
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7You have &ajoined &7breakout chat &f" + chat.name));
    }

    public static synchronized void leaveBreakoutChat(Player player) {
        BreakoutChat chat = getBreakoutChat(player);

        if (chat == null) return;

        if (chat.locked && !player.hasPermission("chat.breakout.leave")) {
            player.sendPlainMessage("You cannot leave your locked breakout chat.");
            return;
        }

        chat.members.remove(player);
        if (chat.members.isEmpty()) {
            closeBreakoutChat(chat);
        }
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7You have &cleft &7breakout chat &f" + chat.name));
    }

    public static synchronized void detachPlayer(Player player) {
        for (BreakoutChat chat : new ArrayList<>(breakoutChats)) {
            if (chat.members.remove(player) && chat.members.isEmpty()) {
                closeBreakoutChat(chat);
            }
        }
    }

    public static synchronized boolean renameChat(BreakoutChat chat, String name) {
        BreakoutChat existing = getBreakoutChat(name);
        if (name.equalsIgnoreCase("main") || (existing != null && existing != chat)) return false;
        chat.name = name;
        return true;
    }

    public static synchronized void enableBreakoutChat(BreakoutChat chat) {
        chat.enabled = true;
        for (Player p : chat.members) {
            p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7The breakout chat you are in has been &are-enabled"));
        }
    }

    public static synchronized void disableBreakoutChat(BreakoutChat chat) {
        chat.enabled = false;
        for (Player p : chat.members) {
            p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7The breakout chat you are in has been &cpaused"));
        }
    }

    public static synchronized BreakoutChat getBreakoutChat(String name) {
        return breakoutChats.stream()
                .filter(c -> c.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static synchronized BreakoutChat getBreakoutChat(Player player) {
        for (BreakoutChat chat : breakoutChats) {
            if (chat.members.contains(player)) {
                return chat;
            }
        }

        return null;
    }
}

