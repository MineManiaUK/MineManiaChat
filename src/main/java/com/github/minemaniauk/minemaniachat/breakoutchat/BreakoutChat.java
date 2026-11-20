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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class BreakoutChat {

    public volatile String name;
    public final Player creator;
    public final List<Player> members = new CopyOnWriteArrayList<>();
    public volatile boolean silent;
    public volatile boolean locked;
    public volatile boolean enabled = true;

    public BreakoutChat(Player creator, Player member, boolean silent, boolean locked) {
        name = member.getUsername() + "@" +  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
        this.creator = creator;
        if (member != creator) {
            this.members.add(creator);
        }
        this.members.add(member);
        this.silent = silent;
        this.locked = locked;
    }

    public void sendMessage(Component message) {
        for (Player p : members) {
            p.sendMessage(p.hasPermission("chat.breakout.notify") || p.hasPermission("chat.breakout.admin")
                    ? Component.text("[Breakout: " + name + "] ").append(message) : message);
        }
        for (Player p : MineManiaChat.getInstance().getProxyServer().getAllPlayers()) {
            if (p.hasPermission("chat.breakout.notify")) {
                if (!members.contains(p)) {
                    p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7(&a" + this.name + "&7) ").append(message));
                }
            }
        }
    }

    public String getName() {
        return name;
    }
}
