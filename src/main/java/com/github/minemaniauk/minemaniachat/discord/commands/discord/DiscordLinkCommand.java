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

import com.github.minemaniauk.minemaniachat.discord.link.LinkManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordLinkCommand extends ListenerAdapter {

    private final LinkManager linkManager;

    public DiscordLinkCommand(LinkManager linkManager) {
        this.linkManager = linkManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("link")) {
            return;
        }

        String discordUserId = event.getUser().getId();

        if (linkManager.isDiscordLinked(discordUserId)) {
            event.reply("Your Discord account is already linked.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String code = event.getOption("code").getAsString();

        boolean linked = linkManager.completeLink(code, discordUserId);

        if (!linked) {
            event.reply("Invalid or expired link code.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.reply("Your Discord account has been linked successfully.")
                .setEphemeral(true)
                .queue();
    }
}
