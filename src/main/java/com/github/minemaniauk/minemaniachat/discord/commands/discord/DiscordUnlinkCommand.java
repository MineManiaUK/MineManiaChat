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

import com.github.minemaniauk.minemaniachat.discord.link.LinkStorage;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.UUID;

public class DiscordUnlinkCommand extends ListenerAdapter {

    private final LinkStorage linkStorage = new LinkStorage();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("unlink")) {
            return;
        }

        String discordUserId = event.getUser().getId();

        UUID minecraftUuid = linkStorage.getMinecraftUuid(discordUserId);

        if (minecraftUuid == null) {
            event.reply("Your Discord account is not linked to a Minecraft account.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        linkStorage.removeLink(minecraftUuid, discordUserId);

        event.reply("Your Discord account has been unlinked from your Minecraft account.")
                .setEphemeral(true)
                .queue();
    }
}
