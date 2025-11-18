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

package com.github.minemaniauk.minemaniachat;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

public class JmSendCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        Player player = (Player) invocation.source();

        for (Player p : MineManiaChat.getInstance().getProxyServer().getAllPlayers()){
            new User(p).sendMessage("&a+ &7" + player.getUsername());
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if (invocation.source() instanceof Player){
           return invocation.source().hasPermission("chat.joinmessage.fakesend");
        }
        return false;
    }
}
