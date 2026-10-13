/*
 * MineManiaChat
 * Used for interacting with the database and message broker.
 *
 * Copyright (C) 2023 MineManiaUK Staff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.minemaniauk.minemaniachat.breakoutchat;

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BreakoutAdminCommand implements SimpleCommand {

    private static final List<String> SUBCOMMANDS = List.of(
            "move",
            "list",
            "open",
            "close",
            "edit",
            "disable",
            "enable"
    );

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            sendUsage(invocation);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "move" -> handleMove(invocation, args);
            case "list" -> handleList(invocation, args);
            case "open" -> handleOpen(invocation, args);
            case "close" -> handleClose(invocation, args);
            case "edit" -> handleEdit(invocation, args);
            case "disable" -> handleDisable(invocation, args);
            case "enable" -> handleEnable(invocation, args);
            default -> sendUsage(invocation);
        }
    }

    private void handleMove(Invocation invocation, String[] args) {
        // /breakouta move <player> <chat>

        if (args.length != 3) {
            invocation.source().sendPlainMessage("Usage: /breakouta move <player> <chat>");
            return;
        }

        String playerName = args[1];
        String chatName = args[2];

        Player player = MineManiaChat.getInstance().getProxyServer().getPlayer(playerName).orElse(null);

        if (player == null) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cPlayer &f" + playerName + " &cdoes not exist"));
            return;
        }

        if (chatName.equals("main")) {
            BreakoutChatManager.removeFromBreakoutChat(player);
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Moved &f" + player.getUsername() + " &7to &fMain"));
            return;
        }

        BreakoutChat chat = BreakoutChatManager.getBreakoutChat(chatName);

        if (chat == null) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cBreakout chat &f" + chatName + " &cdoes not exist"));
            return;
        }

        BreakoutChatManager.addToBreakoutChat(player, chat);
        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Moved &f" + player.getUsername() + " &7to &f" + chat.name));
    }

    private void handleList(Invocation invocation, String[] args) {
        // /breakout list



        if (args.length >= 2) {
            String arg = args[1];
            BreakoutChat chat = BreakoutChatManager.getBreakoutChat(arg);

            invocation.source().sendMessage(Component.text("Players in " + chat.name + ":").decorate(TextDecoration.BOLD));

            for (Player p : chat.members) {
                invocation.source().sendPlainMessage(p.getUsername());
                return;
            }

            invocation.source().sendPlainMessage("No players in chat");
            return;
        }

        invocation.source().sendMessage(Component.text("Breakout chats:").decorate(TextDecoration.BOLD));

        if (BreakoutChatManager.breakoutChats.isEmpty()) {
            invocation.source().sendPlainMessage("No open breakout chats");
            return;
        }

        for (BreakoutChat chat : BreakoutChatManager.breakoutChats) {
            invocation.source().sendPlainMessage(chat.name);
        }
    }

    private void handleOpen(Invocation invocation, String[] args) {
        // /breakouta open <player> <silent> <allowLeave>

        if (invocation.source() instanceof Player executor) {
            if (args.length != 4) {
                invocation.source().sendPlainMessage("Usage: /breakouta open <player> <silent> <allowLeave>");
                return;
            }

            String playerName = args[1];
            boolean silent = Boolean.parseBoolean(args[2]);
            boolean allowLeave = Boolean.parseBoolean(args[3]);

            Player player = MineManiaChat.getInstance().getProxyServer().getPlayer(playerName).orElse(null);

            if (player == null) {
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cPlayer &f" + playerName + " &cdoes not exist"));
                return;
            }

            BreakoutChatManager.openNewChat(executor, player, silent, allowLeave);
        }
    }

    private void handleClose(Invocation invocation, String[] args) {
        // /breakouta close <chat>

        if (args.length != 2) {
            invocation.source().sendPlainMessage("Usage: /breakouta close <chat>");
            return;
        }

        String chatName = args[1];

        BreakoutChat chat = BreakoutChatManager.getBreakoutChat(chatName);

        if (chat == null) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cBreakout chat &f" + chatName + " &cdoes not exist"));
            return;
        }

        BreakoutChatManager.closeBreakoutChat(chat);
        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &cClosed &7breakout chat &f" + chat.name));
    }

    private void handleEdit(Invocation invocation, String[] args) {
        // /breakouta edit <chat> <name|silent|allowmemberleave> <value>

        if (args.length != 4) {
            invocation.source().sendPlainMessage("Usage: /breakouta edit <chat> <name|silent|allowmemberleave> <value>");
            return;
        }

        String chatName = args[1];
        String property = args[2].toLowerCase();
        String value = args[3];

        BreakoutChat chat = BreakoutChatManager.getBreakoutChat(chatName);

        if (chat == null) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cBreakout chat &f" + chatName + " &cdoes not exist"));
            return;
        }

        switch (property) {
            case "name" -> {
                if (BreakoutChatManager.getBreakoutChat(value) != null) {
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cBreakout chat with name &f" + value + " &calready exists"));
                    return;
                }

                chat.name = value;
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Changed &f" + chatName + "&7To &f" + value));
            }

            case "silent" -> {
                boolean silent = Boolean.parseBoolean(value);

                chat.silent = silent;
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Changed &f" + chatName + "&7Silent property to &f" + silent));
            }

            case "allowmemberleave" -> {
                boolean allowMemberLeave = Boolean.parseBoolean(value);

                chat.allowMemberLeave = allowMemberLeave;
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Changed &f" + chatName + "&7Allow Member Leave property to &f" + allowMemberLeave));
            }

            default -> {
                invocation.source().sendMessage(Component.text(">").decorate(TextDecoration.BOLD).color(NamedTextColor.RED).append(Component.text("Invalid property").color(NamedTextColor.RED)));
                invocation.source().sendPlainMessage("Usage: /breakouta edit <chat> <name|silent|allowmemberleave> <value>");
            }
        }
    }

    private void handleDisable(Invocation invocation, String[] args) {
        // /breakouta disable <chat>

        if (args.length != 2) {
            invocation.source().sendPlainMessage("Usage: /breakouta disable <chat>");
            return;
        }

        String chatName = args[1];

        BreakoutChat chat = BreakoutChatManager.getBreakoutChat(chatName);

        if (chat == null) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cBreakout chat &f" + chatName + " &cdoes not exist"));
            return;
        }

        BreakoutChatManager.disableBreakoutChat(chat);
        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &cDisabled &7Breakout chat &f" + chatName));
    }

    private void handleEnable(Invocation invocation, String[] args) {
        // /breakouta enable <chat>

        if (args.length != 2) {
            invocation.source().sendPlainMessage("Usage: /breakouta enable <chat>");
            return;
        }

        String chatName = args[1];

        BreakoutChat chat = BreakoutChatManager.getBreakoutChat(chatName);

        if (chat == null) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cBreakout chat &f" + chatName + " &cdoes not exist"));
            return;
        }

        BreakoutChatManager.enableBreakoutChat(chat);
        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &aEnabled &7Breakout chat &f" + chatName));
    }

    private void sendUsage(Invocation invocation) {
        invocation.source().sendMessage(Component.text("Usage:").decorate(TextDecoration.BOLD));
        invocation.source().sendPlainMessage("/breakouta move <player> <chat>");
        invocation.source().sendPlainMessage("/breakouta list");
        invocation.source().sendPlainMessage("/breakouta open <player> <silent> <allowLeave>");
        invocation.source().sendPlainMessage("/breakouta close <chat>");
        invocation.source().sendPlainMessage("/breakouta edit <chat> <name|silent|allowmemberleave> <value>");
        invocation.source().sendPlainMessage("/breakouta disable <chat>");
        invocation.source().sendPlainMessage("/breakouta enable <chat>");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        /*
         * /breakouta <subcommand>
         */
        if (args.length == 0) {
            return SUBCOMMANDS;
        }

        if (args.length == 1) {
            return filter(SUBCOMMANDS, args[0]);
        }

        String subcommand = args[0].toLowerCase();

        /*
         * /breakouta <subcommand> <arg>
         */
        if (args.length == 2) {
            return switch (subcommand) {
                case "move" -> Stream.concat(
                        suggestChats(args[1]).stream(),
                        Stream.of("main")
                ).toList();

                case "open" -> suggestPlayers(args[1]);

                case "close",
                     "list",
                     "edit",
                     "disable",
                     "enable" -> suggestChats(args[1]);

                default -> List.of();
            };
        }

        /*
         * /breakouta move <player> <chat>
         */
        if (args.length == 3 && subcommand.equals("move")) {
            return suggestChats(args[2]);
        }

        /*
         * /breakouta open <player> <silent> <allowLeave>
         */
        if (args.length == 3 && subcommand.equals("open")) {
            return filter(List.of("true", "false"), args[2]);
        }

        if (args.length == 4 && subcommand.equals("open")) {
            return filter(List.of("true", "false"), args[3]);
        }

        /*
         * /breakouta edit <chat> <property> <value>
         */
        if (args.length == 3 && subcommand.equals("edit")) {
            return filter(
                    List.of(
                            "name",
                            "silent",
                            "allowmemberleave"
                    ),
                    args[2]
            );
        }

        if (args.length == 4 && subcommand.equals("edit")) {
            String property = args[2].toLowerCase();

            if (property.equals("silent")
                    || property.equals("allowmemberleave")) {
                return filter(List.of("true", "false"), args[3]);
            }
        }

        return List.of();
    }

    private List<String> suggestPlayers(String prefix) {
        String lowerPrefix = prefix.toLowerCase();

        return MineManiaChat.getInstance()
                .getProxyServer()
                .getAllPlayers()
                .stream()
                .map(Player::getUsername)
                .filter(username ->
                        username.toLowerCase().startsWith(lowerPrefix)
                )
                .toList();
    }

    private List<String> suggestChats(String prefix) {
       return BreakoutChatManager.breakoutChats.stream()
                .map(BreakoutChat::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                        .toList();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lowerPrefix = prefix.toLowerCase();

        return values.stream()
                .filter(value ->
                        value.toLowerCase().startsWith(lowerPrefix)
                )
                .toList();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source()
                .hasPermission("chat.breakout.admin");
    }
}