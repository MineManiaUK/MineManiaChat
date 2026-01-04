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
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Broadcast implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendPlainMessage("Usage: /broadcast <message>");
            return;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 0, args.length));

        for (Player p : MineManiaChat.getInstance().getProxyServer().getAllPlayers()){
            p.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                            "&f&l[&c&lServer Broadcast&f&l] &a&l" + message
                    )
            );
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.broadcast");
    }
}
