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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SpamCooldown implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length < 2) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cUsage: /mmchatspamcooldown <check|reset> <player>"));
            return;
        }

        Optional<Player> optionalTargetPlayer = MineManiaChat.getInstance().getProxyServer().getPlayer(args[1]);
        if (optionalTargetPlayer.isEmpty()){
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &7The player &f" + args[1] + " &7Could not be found."));
            return;
        }
        Player targetPlayer = optionalTargetPlayer.get();

        var cooldownTime = MineManiaChat.getInstance().getChatHandler().CheckCooldownTime(targetPlayer);

        switch (args[0]){
            case "check":
                if (cooldownTime != 0) {
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7The player &f" + targetPlayer.getUsername() + " &cis cooled-down &7for &f" + cooldownTime + " &7seconds"));
                }
                else {
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7The player &f" + targetPlayer.getUsername() + " &ais not cooled-down"));
                }
                return;
            case "reset":
                if (cooldownTime != 0) {
                    MineManiaChat.getInstance().getChatHandler().playerCooldowns.remove(targetPlayer);
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Removed cooldown from player &f" + targetPlayer.getUsername()));
                }
                else {
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7The player &f" + targetPlayer.getUsername() + " &ais not cooled-down"));
                }
                return;
            default:
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &7Invalid argument please provide &fcheck &7or &reset&7."));
        }

    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return List.of("check", "reset");
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();

            return List.of("check", "reset").stream()
                    .filter(option -> option.startsWith(prefix))
                    .toList();
        }

        if (args.length == 2) {
            Collection<Player> players = MineManiaChat.getInstance()
                    .getProxyServer()
                    .getAllPlayers();

            List<String> completions = new ArrayList<>();
            boolean canSeeVanished = invocation.source().hasPermission("pv.see");

            for (Player player : players) {
                boolean vanished = MineManiaChat.getInstance()
                        .getDbController()
                        .isPlayerVanished(player);

                if (canSeeVanished || !vanished) {
                    completions.add(player.getUsername());
                }
            }

            String prefix = args[1].toLowerCase();

            return completions.stream()
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.manage.spamcooldown");
    }
}
