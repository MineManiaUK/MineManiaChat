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

package com.github.minemaniauk.minemaniachat.discord;

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.github.minemaniauk.minemaniachat.discord.commands.discord.DiscordLinkCommand;
import com.github.minemaniauk.minemaniachat.discord.commands.discord.DiscordUnlinkCommand;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;


public class DiscordManager {

    private JDA jda;
    public Configuration discordConfig;

    public DiscordManager(Configuration discordConfig) {
        this.discordConfig = discordConfig;

        jda = JDABuilder.createDefault(discordConfig.getString("discord-bot-token"))
                .enableIntents(
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_PRESENCES
                )
                .build();
        jda.addEventListener(
                new DiscordListener(),
                new DiscordLinkCommand(MineManiaChat.getInstance().getLinkManager()),
                new DiscordUnlinkCommand()
        );

        jda.updateCommands()
                .addCommands(
                        Commands.slash("link", "Link your Discord account to Minecraft")
                                .addOption(OptionType.STRING, "code", "Your Minecraft link code", true),

                        Commands.slash("unlink", "Unlink your Discord account from Minecraft")
                )
                .queue();
    }

    public void forwardInGameMessage(Player sender, String message) {
        message = escapeLegacyFormatting(message);

        String channelId = discordConfig.getString("active-channel-id");

        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);

        if (channel == null) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(sender.getUsername())
                .setThumbnail("https://mc-heads.net/avatar/" + sender.getUsername())
                .setDescription(message);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void freezeChannel(boolean freeze) {
        String channelId = discordConfig.getString("active-channel-id");

        if (channelId == null || channelId.isBlank()) {
            MineManiaChat.getInstance().getLogger().error("Cannot freeze/unfreeze Discord channel: active-channel-id is missing.");
            return;
        }

        TextChannel channel = jda.getTextChannelById(channelId);

        if (channel == null) {
            MineManiaChat.getInstance().getLogger().error("Cannot freeze/unfreeze Discord channel: no channel found for ID {}.", channelId);
            return;
        }

        Role everyoneRole = channel.getGuild().getPublicRole();

        if (freeze) {
            channel.upsertPermissionOverride(everyoneRole)
                    .deny(Permission.MESSAGE_SEND)
                    .queue();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Discord bridge is disabled")
                    .setDescription("The Discord bridge is currently disabled.")
                    .setColor(Color.RED);

            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            channel.upsertPermissionOverride(everyoneRole)
                    .clear(Permission.MESSAGE_SEND)
                    .queue();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Discord bridge now enabled")
                    .setDescription("The Discord bridge is now enabled.")
                    .setColor(Color.GREEN);

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    public JDA getJda() {
        return this.jda;
    }

    public Configuration getDiscordConfig() {
        return this.discordConfig;
    }

    public void Shutdown(){
        jda.shutdownNow();
    }

    private String escapeLegacyFormatting(String message) {
        return message.replace("&", "＆").replace("§", "§\u200B");
    }
}
