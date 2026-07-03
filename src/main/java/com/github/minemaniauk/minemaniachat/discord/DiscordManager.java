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
import com.github.minemaniauk.minemaniachat.discord.commands.discord.DiscordPlayersCommand;
import com.github.minemaniauk.minemaniachat.discord.commands.discord.DiscordUnlinkCommand;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.util.Locale;


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
                new DiscordUnlinkCommand(),
                new DiscordPlayersCommand()
        );

        jda.updateCommands()
                .addCommands(
                        Commands.slash("link", "Link your Discord account to Minecraft")
                                .addOption(OptionType.STRING, "code", "Your Minecraft link code", true),

                        Commands.slash("unlink", "Unlink your Discord account from Minecraft"),

                        Commands.slash("players", "Replies with a list of online players")
                )
                .queue(
                        success -> System.out.println("Registered global slash commands."),
                        error -> error.printStackTrace()
                );

        Object rawType = discordConfig.get("presence.type");

        Activity.ActivityType activityType = Activity.ActivityType.PLAYING;

        if (rawType instanceof String typeName && !typeName.isBlank()) {
            try {
                activityType = Activity.ActivityType.valueOf(typeName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                activityType = Activity.ActivityType.PLAYING;
            }
        }

        String activityText = discordConfig.getString("presence.text");

        if (activityType != null && activityText != null) {
            jda.getPresence().setPresence(Activity.of(activityType, activityText), false);
            jda.getPresence().setActivity(null);
        }
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

    public void setPresence(Activity.ActivityType activityType, String text) {
        discordConfig.set("presence.type", activityType.toString().toLowerCase());
        discordConfig.set("presence.text", text);
        discordConfig.save();

        jda.getPresence().setPresence(Activity.of(activityType, text), false);
    }

    public void ClearPresence() {
        discordConfig.set("presence", null);
        discordConfig.save();

        jda.getPresence().setActivity(null);
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

    public void sendJoinMessage(Player player) {
        String channelId = discordConfig.getString("active-channel-id");

        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);

        if (channel == null) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Player joined")
                .setThumbnail("https://mc-heads.net/avatar/" + player.getUsername())
                .setDescription(player.getUsername() + " Joined the server")
                .setColor(Color.GREEN);

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public void sendLeaveMessage(Player player) {
        String channelId = discordConfig.getString("active-channel-id");

        MessageChannel channel = jda.getChannelById(MessageChannel.class, channelId);

        if (channel == null) {
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Player left")
                .setThumbnail("https://mc-heads.net/avatar/" + player.getUsername())
                .setDescription(player.getUsername() + " Left the server")
                .setColor(Color.RED);

        channel.sendMessageEmbeds(embed.build()).queue();
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
