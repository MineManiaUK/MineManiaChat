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

package com.github.minemaniauk.minemaniachat.discord.commands.discord;

import com.github.minemaniauk.minemaniachat.commands.ListCommmand;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.model.group.Group;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiscordPlayersCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("players")) {
            return;
        }

        String playerList = getPlayerList();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Online Players")
                .setColor(Color.CYAN)
                .setDescription(playerList.isEmpty() ? "**No players online.**" : playerList);
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private String getPlayerList() {
        Map<Group, List<Player>> groupPlayerMap = ListCommmand.getGroupPlayerMap();
        StringBuilder output = new StringBuilder();

        for (Map.Entry<Group, List<Player>> entry : groupPlayerMap.entrySet()) {
            Group group = entry.getKey();

            String prefix = group.getCachedData()
                    .getMetaData()
                    .getPrefix();

            prefix = PlainTextComponentSerializer.plainText().serialize(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(prefix)
            );

            output.append(prefix)
                    .append(":\n ");

            String playerNames = entry.getValue().stream()
                    .map(player -> "`" + player.getUsername() + "`")
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.joining("\n"));

            output.append(playerNames)
                    .append("\n\n");
        }

        return output.toString();
    }
}
