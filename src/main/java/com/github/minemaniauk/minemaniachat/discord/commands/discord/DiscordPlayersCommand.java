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

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.Collection;
import java.util.stream.Collectors;

public class DiscordPlayersCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("players")) {
            return;
        }

        Collection<Player> players = MineManiaChat.getInstance().getProxyServer().getAllPlayers();

        String playerList = players.stream()
                .filter(player -> !MineManiaChat.getInstance()
                        .getDbController()
                        .isPlayerVanished(player))
                .map(player -> "`" + player.getUsername() + "`")
                .collect(Collectors.joining("\n"));

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Online Players")
                .setColor(Color.CYAN)
                .setDescription(playerList.isEmpty() ? "**No players online.**" : playerList);
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

}
