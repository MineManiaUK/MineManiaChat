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
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.github.smuddgge.squishyconfiguration.interfaces.ConfigurationSection;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.awt.*;
import java.util.List;

public class DiscordListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(MineManiaChat.getInstance().getDiscordHandler().getDiscordConfig().getString("active-channel-id"))) return;

        String message = event.getMessage().getContentDisplay();
        Member member  = event.getMember();
        Roles memberRole = getHighestConfiguredRole(member, MineManiaChat.getInstance().getDiscordHandler().getDiscordConfig());
        if (MineManiaChat.getInstance().getChatHandler().containsBannedWords(message)) {
            event.getMessage().delete().queue();
            return;
        }

        if (MineManiaChat.getInstance().getChatHandler().URL_PATTERN.matcher(message).find()) {
            if (memberRole == null || !memberRole.isAtLeast(Roles.HELPER)) {
                event.getMessage().delete().queue();

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Message could not be delivered")
                        .setDescription("Try removing any URLs from your message.")
                        .setColor(Color.RED);

                event.getMessage().replyEmbeds(embed.build()).queue();
                return;
            }
        }

        if (!MineManiaChat.getInstance().getConfig().getBoolean("chat-enabled")){
            if (memberRole == null || !memberRole.isAtLeast(Roles.HELPER)) {
                event.getMessage().delete().queue();

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Message could not be delivered")
                        .setDescription("Chat is disabled on the server")
                        .setColor(Color.RED);

                event.getMessage().replyEmbeds(embed.build()).queue();
                return;
            }
        }

        if (!MineManiaChat.getInstance().getConfig().getBoolean("discord-enabled") || !MineManiaChat.getInstance().getConfig().getBoolean("chat-enabled")){
            if (memberRole == null || !memberRole.isAtLeast(Roles.HELPER)) {
                event.getMessage().delete().queue();

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Message could not be delivered")
                        .setDescription("The discord bridge is currently disabled")
                        .setColor(Color.RED);

                event.getMessage().replyEmbeds(embed.build()).queue();
                return;
            }
        }

        String chatReadyMessage = formatMessage(member, message, memberRole);

        for (Player p : MineManiaChat.getInstance().getProxyServer().getAllPlayers()) {
            p.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                            chatReadyMessage
                    )
            );
        }

        MineManiaChat.getInstance().getLogger().info(
                LegacyComponentSerializer.legacyAmpersand().deserialize(
                        chatReadyMessage
                )
        );

    }

    private String formatMessage(Member member, String message, Roles memberRole) {
        String formatedMessage = MineManiaChat.getInstance().getDiscordHandler().getDiscordConfig().getString("discord-prefix");;
        ConfigurationSection rolesSections = MineManiaChat.getInstance().getDiscordHandler().getDiscordConfig().getSection("permissions.role");

        if (memberRole == null) {
            memberRole = Roles.MEMBER;
        }

        switch (memberRole) {
            case OWNER:
                formatedMessage += " " + rolesSections.getString("owner.prefix") + " &f" + member.getEffectiveName() + " &7:" + " &f" + message;
                break;
            case ADMIN:
                formatedMessage += " " + rolesSections.getString("admin.prefix") + " &f" + member.getEffectiveName() + " &7:" + " &f" + message;
                break;
            case HELPER:
                formatedMessage += " " + rolesSections.getString("helper.prefix") + " &f" + member.getEffectiveName() + " &7:" + " &f" + message;
                break;
            case MEMBER:
                formatedMessage += " &f" + member.getEffectiveName() + " &7:" + " &f" + escapeLegacyFormatting(message);
                break;
        }

        return formatedMessage;
    }

    private Roles getHighestConfiguredRole(Member member, Configuration config) {
        String ownerRoleId = config.getString("permissions.role.owner.id");
        String adminRoleId = config.getString("permissions.role.admin.id");
        String helperRoleId = config.getString("permissions.role.helper.id");
        String memberRoleId = config.getString("permissions.role.member.id");

        List<Role> roles = member.getRoles();

        if (hasRole(roles, ownerRoleId)) {
            return Roles.OWNER;
        }

        if (hasRole(roles, adminRoleId)) {
            return Roles.ADMIN;
        }

        if (hasRole(roles, helperRoleId)) {
            return Roles.HELPER;
        }

        if (hasRole(roles, memberRoleId)) {
            return Roles.MEMBER;
        }

        return null;
    }

    private boolean hasRole(List<Role> roles, String roleId) {
        if (roleId == null || roleId.isBlank()) {
            return false;
        }

        return roles.stream()
                .anyMatch(role -> role.getId().equals(roleId));
    }

    private String escapeLegacyFormatting(String message) {
        return message.replace("&", "＆").replace("§", "§\u200B");
    }
}