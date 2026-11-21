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
import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.nio.file.Path;

public class PlayerBannedWords implements SimpleCommand {
    private final Path playerDataPath;

    public PlayerBannedWords(Path playerDataPath) {
        this.playerDataPath = playerDataPath;
    }

    private Configuration loadPlayerFile(UUID id) {
        Configuration playerFile = ConfigurationFactory.YAML.create(playerDataPath.toFile(), id.toString());
        playerFile.load();
        return playerFile;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length < 3) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cUsage: /playerbannedwords <player> <add|check|remove> <word>"));
            return;
        }

        Player targetPlayer = MineManiaChat.getInstance().getProxyServer().getPlayer(args[0]).orElse(null);
        if (targetPlayer == null) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &7The player &f" + args[0] + " &7could not be found."));
            return;
        }
        UUID playerId = targetPlayer.getUniqueId();
        String wordInput = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        switch (args[1].toLowerCase(Locale.ROOT)){
            case "add":
                try {
                    Configuration bannedWordConfig = loadPlayerFile(playerId);
                    List<String> bannedWords = new ArrayList<>(bannedWordConfig.getListString("banned-words", List.of()));
                    if (!bannedWords.contains(wordInput)){
                        bannedWords.add(wordInput);
                        bannedWordConfig.set("banned-words", bannedWords);
                        bannedWordConfig.save();
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Successfully added \"" + "&c" + wordInput + "&7" + "\" to &f" + targetPlayer.getUsername() + " &7banned words list"));
                    }
                    else {
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &7Could not add \"" + "&f" + wordInput + "&7" + "\" to &f" + targetPlayer.getUsername() + " &7banned words list because it is already on the list"));
                    }
                }
                catch (Exception e) {
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cAn error occurred. No changes have been made"));
                    MineManiaChat.getInstance().getLogger().atError().setCause(e).log("An error occurred when appending a word to the banned words list");
                }
                return;
            case "check":
                try {
                    var result = MineManiaChat.getInstance().getChatHandler().checkBannedWords(wordInput, playerId);
                    if (result.isBlocked()){
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7\"" + "&f" + wordInput + "&7" + "\" is &cbanned &7by the &f" + result.source()));
                    }
                    else {
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7\"" + "&f" + wordInput + "&7" + "\" is &anot banned"));
                    }
                }
                catch (Exception e) {
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cAn error occurred."));
                    MineManiaChat.getInstance().getLogger().atError().setCause(e).log("An error occurred when checking if a word was banned");
                }
                return;

            case "remove":
                try {
                    Configuration bannedWordConfig = loadPlayerFile(playerId);
                    List<String> bannedWords = new ArrayList<>(bannedWordConfig.getListString("banned-words", List.of()));
                    if (bannedWords.contains(wordInput)){
                        bannedWords.remove(wordInput);
                        bannedWordConfig.set("banned-words", bannedWords);
                        bannedWordConfig.save();
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Successfully removed \"" + "&a" + wordInput + "&7" + "\" from &f" + targetPlayer.getUsername() + " &7banned words list"));
                    }
                    else {
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &7Could not remove \"" + "&f" + wordInput + "&7" + "\" from &f" + targetPlayer.getUsername() + " &7banned words list because it is not on the list"));
                    }
                }
                catch (Exception e) {
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cAn error occurred. No changes have been made"));
                    MineManiaChat.getInstance().getLogger().atError().setCause(e).log("An error occurred when removing a word from the banned words list");
                }
                return;

            default:
                invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &7Invalid argument please provide &fadd&7, &fcheck &7or &fremove&7."));
                return;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length <= 1) {
            String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
            return MineManiaChat.getInstance().getProxyServer().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        }

        if (args.length == 2) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return List.of("add", "check", "remove").stream()
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("remove")) {
            String prefix = args[2].toLowerCase(Locale.ROOT);
            if (prefix.isBlank()) return List.of();

            Player targetPlayer = MineManiaChat.getInstance().getProxyServer().getPlayer(args[0]).orElse(null);
            if (targetPlayer == null) return List.of();
            List<String> completions = loadPlayerFile(targetPlayer.getUniqueId())
                    .getListString("banned-words", List.of());

            return completions.stream()
                    .filter(w -> w.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.bannedwords");
    }
}
