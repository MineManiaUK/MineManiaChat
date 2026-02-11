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

package com.github.minemaniauk.minemaniachat.commands;

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatDisable implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        try {
            Configuration config = MineManiaChat.getInstance().getConfig();
            config.set("chat-enabled", false);
            config.save();
            MineManiaChat.getInstance().reloadConfigs();
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Successfully &cdisabled &7chat"));
        }
        catch (Exception e) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cAn error occurred. No changes have been made."));
            MineManiaChat.getInstance().getLogger().atError().setCause(e).log("An error occurred when disabling chat");
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.disable");
    }
}
