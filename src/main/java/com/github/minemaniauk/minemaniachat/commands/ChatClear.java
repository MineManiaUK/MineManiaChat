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

package com.github.minemaniauk.minemaniachat.commands;

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.github.minemaniauk.minemaniachat.User;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.Collection;

public class ChatClear implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        ClearChat();
    }


    private void ClearChat(){
        Collection<Player> allPlayers = MineManiaChat.getInstance().getProxyServer().getAllPlayers();
        int lines = MineManiaChat.getInstance().getConfig().getInteger("clear-chat-lines");

        for (Player p : allPlayers){
            User u = new User(p);
            for (int i = 0; i < lines; i++) {
                p.sendMessage(Component.empty());
            }
            u.sendMessage("&7&l> §7Chat has been cleared.");
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.clear");
    }
}
