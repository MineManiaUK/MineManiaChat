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

package com.github.minemaniauk.minemaniachat.message.commands;

import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.github.minemaniauk.minemaniachat.User;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Message implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();
        if (args.length < 2) {
            invocation.source().sendPlainMessage("Usage: /msg <player> <message>");
            return;
        }

        String targetName = args[0];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        Optional<Player> recipient = MineManiaChat.getInstance().getProxyServer().getPlayer(targetName);

        if (!recipient.isPresent()) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cAn error occurred"));
            MineManiaChat.getInstance().getLogger().warn("Could not get recipient");
            return;
        }

        if (invocation.source() instanceof Player player){
            if (player == recipient.get()){
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cYou can't message yourself"));
                return;
            }

            MineManiaChat.getInstance().getMessageHandler().sendPlayerMessage(player, recipient.get(), message);
        }
        else if (invocation.source() instanceof ConsoleCommandSource) {
            MineManiaChat.getInstance().getMessageHandler().sendConsoleMessage(recipient.get(), message);
        }
        else {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cAn error occurred"));
            MineManiaChat.getInstance().getLogger().warn("Could not get message source cancelling");
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {

        if (invocation.arguments().length > 1) return List.of();

        Collection<Player> players = MineManiaChat.getInstance()
                .getProxyServer()
                .getAllPlayers();

        List<String> completions = new ArrayList<>();
        for (Player player : players) {
            completions.add(player.getUsername());
        }

        String[] args = invocation.arguments();
        String prefix = args.length > 0 ? args[0].toLowerCase() : "";


        return completions.stream()
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .toList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.private-message.allow");
    }

}
