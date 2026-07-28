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

import com.github.minemaniauk.minemaniachat.DataBaseController;
import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.*;
import java.util.stream.Collectors;

public class ListCommmand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        StringBuilder output = new StringBuilder("&f&lPlayers:\n\n");
        Map<Group, List<Player>> groupPlayerMap = getGroupPlayerMap();

        for (Map.Entry<Group, List<Player>> entry : groupPlayerMap.entrySet()) {
            Group group = entry.getKey();

            String prefix = group.getCachedData()
                    .getMetaData()
                    .getPrefix();

            if (prefix == null) {
                prefix = "&7" + group.getName();
            }

            output.append(prefix)
                    .append("\n");

            entry.getValue().stream()
                    .map(Player::getUsername)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(playerName -> output
                            .append("&7- &f")
                            .append(playerName)
                            .append("\n"));

            output.append("\n");
        }

        invocation.source().sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(output.toString())
        );
    }

    public static Map<Group, List<Player>> getGroupPlayerMap() {
        LuckPerms luckPerms = LuckPermsProvider.get();
        DataBaseController dbController =
                MineManiaChat.getInstance().getDbController();

        Map<Group, List<Player>> unsortedGroups = new HashMap<>();

        for (Player player : MineManiaChat.getInstance()
                .getProxyServer()
                .getAllPlayers()) {

            boolean vanished = dbController != null
                    && dbController.isPlayerVanished(player);

            if (vanished) {
                continue;
            }

            User user = luckPerms.getUserManager()
                    .getUser(player.getUniqueId());

            if (user == null) {
                continue;
            }

            Group group = luckPerms.getGroupManager()
                    .getGroup(user.getPrimaryGroup());

            if (group == null) {
                continue;
            }

            unsortedGroups
                    .computeIfAbsent(group, ignored -> new ArrayList<>())
                    .add(player);
        }

        return unsortedGroups.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(
                        (Map.Entry<Group, List<Player>> entry) ->
                                entry.getKey().getWeight().orElse(0)
                ).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }
}
