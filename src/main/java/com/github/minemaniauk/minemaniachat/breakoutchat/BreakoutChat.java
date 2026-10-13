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
import java.util.ArrayList;
import java.util.List;

public class BreakoutChat {

    public String name;
    public final Player creator;
    public List<Player> members = new ArrayList<>();
    public boolean silent;
    public boolean allowMemberLeave;
    public boolean enabled = true;

    public BreakoutChat(Player creator, Player member, boolean silent, boolean allowMemberLeave) {
        name = member.getUsername() + "@" +  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
        this.creator = creator;
        if (member != creator) {
            this.members.add(creator);
        }
        this.members.add(member);
        this.silent = silent;
        this.allowMemberLeave = allowMemberLeave;
    }

    public void sendMessage(Component message) {
        if (!enabled) return;

        for (Player p : members) {
            p.sendMessage(message);
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
