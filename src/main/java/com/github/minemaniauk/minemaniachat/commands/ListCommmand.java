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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListCommmand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        StringBuilder output = new StringBuilder("&f&lPlayers:\n\n");
        HashMap<Group, List<Player>> groupPlayerMap = getGroupPlayerMap();

        for (Map.Entry<Group, List<Player>> entry : groupPlayerMap.entrySet()) {
            Group group = entry.getKey();

            String prefix = group.getCachedData()
                    .getMetaData()
                    .getPrefix();

            if (prefix == null) {
                prefix = "&7" + group.getName();
            }

            output.append(prefix)
                    .append("&7: &f");

            String playerNames = entry.getValue().stream()
                    .map(Player::getUsername)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.joining("&f, "));

            output.append(playerNames)
                    .append("\n\n");
        }

        invocation.source().sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(output.toString()));
    }

    public static HashMap<Group, List<Player>> getGroupPlayerMap() {
        LuckPerms lp = LuckPermsProvider.get();
        HashMap<Group, List<Player>> groupPlayerMap = new HashMap<>();
        DataBaseController dbController = MineManiaChat.getInstance().getDbController();

        for (Player p : MineManiaChat.getInstance().getProxyServer().getAllPlayers()) {
            boolean vanished = dbController != null
                    && dbController.isPlayerVanished(p);
            if (!vanished){
                User lpUser = lp.getUserManager().getUser(p.getUniqueId());
                Group userGroup = lp.getGroupManager().getGroup(lpUser.getPrimaryGroup());

                groupPlayerMap
                        .computeIfAbsent(userGroup, ignored -> new ArrayList<>())
                        .add(p);
            }
        }

        return groupPlayerMap;
    }
}
