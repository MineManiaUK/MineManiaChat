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
import com.velocitypowered.api.command.SimpleCommand;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public class DiscordPresence implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {

        String[] args = invocation.arguments();

        if (args.length < 1) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cUsage: /mmchatdiscord <enable|disable>"));
            return;
        }

        String arg = invocation.arguments()[0];
        String text = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        ActivityType activityType = null;

        switch (arg){
            case "playing":
                activityType = ActivityType.PLAYING;
                break;
            case "streaming":
                activityType = ActivityType.STREAMING;
                break;
            case "listening":
                activityType = ActivityType.LISTENING;
                break;
            case "watching":
                activityType = ActivityType.WATCHING;
                break;
            case "custom":
                activityType = ActivityType.CUSTOM_STATUS;
                break;
            case "competing":
                activityType = ActivityType.COMPETING;
                break;
            case "clear":
                MineManiaChat.getInstance().getDiscordManager().ClearPresence();
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &aSuccessfully &ccleared &7the bots discord presence"));
                return;
        }

        MineManiaChat.getInstance().getDiscordManager().setPresence(activityType, text);
        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &aSuccessfully &7Set the bots discord presence"));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> completions = List.of("playing", "streaming", "listening", "watching", "custom", "competing", "clear");

        if (args.length == 0) {
            return completions;
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();

            return completions.stream()
                    .filter(option -> option.startsWith(prefix))
                    .toList();
        }


        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.manage.discordpresence");
    }
}
