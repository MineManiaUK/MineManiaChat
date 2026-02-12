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

import java.util.List;

public class BannedWords implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length < 2) {
            invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cUsage: /mmchatbannedwords <add|check|remove> <word>"));
            return;
        }

        String wordInput = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        switch (args[0]){
            case "add":
                try {
                    Configuration bannedWordConfig = MineManiaChat.getInstance().getBannedWords();
                    List<String> bannedWords = bannedWordConfig.getListString("banned-words");
                    if (!bannedWords.contains(wordInput)){
                        bannedWords.add(wordInput);
                        MineManiaChat.getInstance().getBannedWords().set("banned-words", bannedWords);
                        bannedWordConfig.save();
                        MineManiaChat.getInstance().reloadBannedWords();
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Successfully added \"" + "&c" + wordInput + "&7" + "\" to the banned words list"));
                    }
                    else {
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &7Could not add \"" + "&f" + wordInput + "&7" + "\" to the banned words list because it is already on the list"));
                    }
                }
                catch (Exception e) {
                    invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&l> &cAn error occurred. No changes have been made"));
                    MineManiaChat.getInstance().getLogger().atError().setCause(e).log("An error occurred when appending a word to the banned words list");
                }
                return;
            case "check":
                try {
                    Configuration bannedWordConfig = MineManiaChat.getInstance().getBannedWords();
                    List<String> bannedWords = bannedWordConfig.getListString("banned-words");
                    boolean wordContained = bannedWords.contains(wordInput);
                    if (wordContained){
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7\"" + "&f" + wordInput + "&7" + "\" is &cbanned"));
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
                    Configuration bannedWordConfig = MineManiaChat.getInstance().getBannedWords();
                    List<String> bannedWords = bannedWordConfig.getListString("banned-words");
                    if (bannedWords.contains(wordInput)){
                        bannedWords.remove(wordInput);
                        MineManiaChat.getInstance().getBannedWords().set("banned-words", bannedWords);
                        bannedWordConfig.save();
                        MineManiaChat.getInstance().reloadBannedWords();
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l> &7Successfully removed \"" + "&a" + wordInput + "&7" + "\" from the banned words list"));
                    }
                    else {
                        invocation.source().sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c&c&l> Could not remove \"" + "&f" + wordInput + "&7" + "\" from the banned words list because it is not on the list"));
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

        if (args.length == 0) {
            return List.of("add", "check", "remove");
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return List.of("add", "check", "remove").stream()
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String prefix = args[1].toLowerCase();
            if (prefix.isBlank()) return List.of();

            List<String> completions = MineManiaChat.getInstance()
                    .getBannedWords()
                    .getListString("banned-words");

            return completions.stream()
                    .filter(w -> w.toLowerCase().startsWith(prefix))
                    .toList();
        }

        return List.of();
    }


    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.bannedwords");
    }
}
