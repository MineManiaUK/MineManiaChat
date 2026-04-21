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
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BroadcastServer implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendPlainMessage("Usage: /broadcastserver <server> <message>");
            return;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        Optional<RegisteredServer> optionalServer = MineManiaChat.getInstance().getProxyServer().getServer(args[0]);

        if (optionalServer.isPresent()){
            RegisteredServer server = optionalServer.get();
            for (Player p : server.getPlayersConnected()){
                p.sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize("&f&l[&c&lServer Broadcast&f&l] &a&l" + message)
                );
            }
            invocation.source().sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Successfully broadcasted to &f" + server.getServerInfo().getName())
            );
        }
        else {
            invocation.source().sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cThe inputted server could not be found")
            );
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length > 1) return List.of();

        List<String> completions = new ArrayList<>();
        for (RegisteredServer server : MineManiaChat.getInstance().getProxyServer().getAllServers()) {
            completions.add(server.getServerInfo().getName());
        }

        String[] args = invocation.arguments();
        String prefix = args.length > 0 ? args[0].toLowerCase() : "";

        return completions.stream()
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .toList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.broadcast");
    }
}
