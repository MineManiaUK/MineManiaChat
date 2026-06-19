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
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public class DiscordCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length < 1) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cUsage: /mmchatdiscord <enable|disable>"));
            return;
        }

        String arg = invocation.arguments()[0];
        Configuration config = MineManiaChat.getInstance().getConfig();

        switch (arg){
            case "enable":
                config.set("discord-enabled", true);
                config.save();
                MineManiaChat.getInstance().getDiscordHandler().freezeChannel(false);
                MineManiaChat.getInstance().reloadConfigs();
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Successfully &aenabled &7discord bridge"));
                break;
            case "disable":
                config.set("discord-enabled", false);
                config.save();
                MineManiaChat.getInstance().getDiscordHandler().freezeChannel(true);
                MineManiaChat.getInstance().reloadConfigs();
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Successfully &cdisabled &7discord bridge"));
                break;
            default:
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cUsage: /mmchatdiscord <enable|disable>"));
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return List.of("enable", "disable");
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();

            return List.of("enable", "disable").stream()
                    .filter(option -> option.startsWith(prefix))
                    .toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.discordcommand");
    }
}
