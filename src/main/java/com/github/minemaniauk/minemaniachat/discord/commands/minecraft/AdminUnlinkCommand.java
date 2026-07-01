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

package com.github.minemaniauk.minemaniachat.discord.commands.minecraft;

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.github.minemaniauk.minemaniachat.discord.link.LinkStorage;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;

public class AdminUnlinkCommand implements SimpleCommand {

    private final LinkStorage linkStorage;

    public AdminUnlinkCommand(LinkStorage linkStorage) {
        this.linkStorage = linkStorage;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cUsage: /forceunlink <player>"));
            return;
        }

        Optional<Player> optionalPlayer = MineManiaChat.getInstance().getProxyServer().getPlayer(invocation.arguments()[0]);

        if (optionalPlayer.isEmpty()){
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cCould not find player"));
            return;
        }

        Player player = optionalPlayer.get();

        UUID minecraftUuid = player.getUniqueId();

        String discordUserId = linkStorage.getDiscordId(minecraftUuid);

        if (discordUserId == null) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &7The target Minecraft account &cis not linked &7to a Discord account."));
            return;
        }

        linkStorage.removeLink(minecraftUuid, discordUserId);

        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cThe target Minecraft account has been unlinked from Discord."));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (invocation.arguments().length > 1) return List.of();

        Collection<Player> players = MineManiaChat.getInstance()
                .getProxyServer()
                .getAllPlayers();

        String[] args = invocation.arguments();
        String prefix = args.length > 0 ? args[0].toLowerCase() : "";

        List<String> completions = new ArrayList<>();
        for (Player player : players) {
            completions.add(player.getUsername());
        }

        return completions.stream()
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .toList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.manage.discord");
    }
}
