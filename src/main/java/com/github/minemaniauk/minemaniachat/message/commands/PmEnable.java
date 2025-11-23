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

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PmEnable implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendPlainMessage("Usage: /enablepm <Player|global>");
            return;
        }

        if (invocation.arguments()[0].equals("global")){
            if (!MineManiaChat.getInstance().getDataManager().isGlobalPmsEnabled()){
                MineManiaChat.getInstance().getDataManager().globalEnablePms();
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &aEnabled &7PMs globally"));
                return;
            }
            else {
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cPMs already globally enabled"));
                return;
            }
        }

        Optional<Player> recipient = MineManiaChat.getInstance().getProxyServer().getPlayer(invocation.arguments()[0]);

        if (recipient.isPresent()){
            if (!MineManiaChat.getInstance().getDataManager().isPlayerPmFlagEnabled(recipient.get())){
                MineManiaChat.getInstance().getDataManager().enablePmsUser(recipient.get());
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &aEnabled &7PMs for " + recipient.get().getUsername()));
            }
            else {
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cPMs already enabled for player"));
            }
        }
        else {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cCould not find player"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {

        if (invocation.arguments().length > 1) return List.of();

        Collection<Player> players = MineManiaChat.getInstance()
                .getProxyServer()
                .getAllPlayers();

        List<String> completions = new ArrayList<>();
        completions.add("global");
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
        return invocation.source().hasPermission("chat.private-message.mute");
    }
}
