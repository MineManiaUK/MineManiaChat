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

import com.github.minemaniauk.minemaniachat.discord.link.LinkManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class LinkCommand implements SimpleCommand {

    private final LinkManager linkManager;

    public LinkCommand(LinkManager linkManager) {
        this.linkManager = linkManager;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("Only players can use this command."));
            return;
        }

        if (linkManager.isMinecraftLinked(player.getUniqueId())) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cYour Minecraft account is already linked."));
            return;
        }

        String code = linkManager.createLinkCode(player);

        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &fYour Discord link code is: &b&l" + code));
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &fGo to Discord and run: &l/link " + code));
    }
}