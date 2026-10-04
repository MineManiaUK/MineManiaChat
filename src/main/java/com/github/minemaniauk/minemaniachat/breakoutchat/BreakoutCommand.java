package com.github.minemaniauk.minemaniachat.breakoutchat;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public class BreakoutCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player p)) {
            return;
        }

        String[] args = invocation.arguments();

        if (args.length == 0) {
            invocation.source().sendPlainMessage("Usage: /breakout <join|leave> [name]");
            return;
        }

        if (args[0].equalsIgnoreCase("join")) {
            if (args.length < 2) {
                invocation.source().sendPlainMessage("Usage: /breakout join <name>");
                return;
            }

            if (!p.hasPermission("chat.breakout.join")) {
                invocation.source().sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                                "&c&l> &cYou can not join this breakout chat"
                        )
                );
                return;
            }

            BreakoutChat chat = BreakoutChatManager.breakoutChats.stream()
                    .filter(c -> c.name.equalsIgnoreCase(args[1]))
                    .findFirst()
                    .orElse(null);

            if (chat == null) {
                invocation.source().sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                                "&c&l> &cBreakout chat &f" + args[1] + " &cdoes not exist"
                        )
                );
                return;
            }

            BreakoutChatManager.joinBreakoutChat(p, chat);
            return;
        }

        if (args[0].equalsIgnoreCase("leave")) {
            if (BreakoutChatManager.getBreakoutChat(p) == null) {
                invocation.source().sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(
                                "&c&l> &cYou are not in a breakout chat"
                        )
                );
                return;
            }

            BreakoutChatManager.leaveBreakoutChat(p);
            return;
        }

        invocation.source().sendPlainMessage("Usage: /breakout <join|leave> [name]");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            return List.of("join", "leave");
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();

            return List.of("join", "leave").stream()
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("join")
                && invocation.source().hasPermission("chat.breakout.join")) {

            String prefix = args[1].toLowerCase();

            return BreakoutChatManager.breakoutChats.stream()
                    .map(chat -> chat.name)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .toList();
        }

        return List.of();
    }
}