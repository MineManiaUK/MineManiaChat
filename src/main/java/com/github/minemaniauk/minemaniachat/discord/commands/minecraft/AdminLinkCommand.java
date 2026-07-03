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
import com.github.minemaniauk.minemaniachat.discord.link.LinkManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class AdminLinkCommand implements SimpleCommand {

    private final LinkManager linkManager;

    public AdminLinkCommand(LinkManager linkManager) {
        this.linkManager = linkManager;
    }

    @Override
    public void execute(Invocation invocation) {

        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("Only players can use this command."));
            return;
        }

        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cUsage: /discordadminlink <player>"));
            return;
        }

        Optional<Player> optionalPlayer = MineManiaChat.getInstance().getProxyServer().getPlayer(invocation.arguments()[0]);

        if (optionalPlayer.isEmpty()){
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cCould not find player"));
            return;
        }

        Player player = optionalPlayer.get();

        if (linkManager.isMinecraftLinked(player.getUniqueId())) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cA discord account is already linked to this Minecraft account."));
            return;
        }

        String code = linkManager.createLinkCode(player);

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &fAn admin has activated a discord link for you"));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &fYour Discord link code is: &b&l" + code));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &fGo to Discord and run: &l/link " + code));

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
