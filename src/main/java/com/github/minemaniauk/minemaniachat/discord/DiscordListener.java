package com.github.minemaniauk.minemaniachat.discord;

import com.github.minemaniauk.minemaniachat.MineManiaChat;
import com.github.smuddgge.squishyconfiguration.interfaces.ConfigurationSection;
import com.velocitypowered.api.proxy.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DiscordListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        if (!event.getChannel().getId().equals(
                MineManiaChat.getInstance()
                        .getDiscordHandler()
                        .getDiscordConfig()
                        .getString("active-channel-id")
        )) {
            return;
        }

        String message = event.getMessage().getContentDisplay();
        Member member = event.getMember();

        if (member == null) {
            return;
        }

        if (MineManiaChat.getInstance().getChatHandler().containsBannedWords(message)) {
            event.getMessage().delete().queue();
            return;
        }

        if (!MineManiaChat.getInstance().getLinkManager().isDiscordLinked(member.getId())) {
            event.getMessage().delete().queue();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Message could not be delivered")
                    .setDescription("Your discord account is not linked run /link on the minecraft server and follow the instructions")
                    .setColor(Color.RED);

            event.getMessage().replyEmbeds(embed.build()).queue();
            return;
        }

        UUID minecraftUuid = MineManiaChat.getInstance()
                .getLinkManager()
                .getMinecraftUuid(member.getId());

        if (minecraftUuid == null) {
            event.getMessage().delete().queue();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Message could not be delivered")
                    .setDescription("ERROR: linked Minecraft UUID is null")
                    .setColor(Color.RED);

            event.getMessage().replyEmbeds(embed.build()).queue();
            return;
        }

        String minecraftUsername = MineManiaChat.getInstance()
                .getLinkManager()
                .getMinecraftUsername(minecraftUuid);

        if (minecraftUsername == null || minecraftUsername.isBlank()) {
            minecraftUsername = minecraftUuid.toString();
        }

        handleDiscordMessage(event, member, minecraftUuid, minecraftUsername, message);
    }

    private void handleDiscordMessage(
            MessageReceivedEvent event,
            Member member,
            UUID minecraftUuid,
            String minecraftUsername,
            String message
    ) {
        boolean hasUrl = MineManiaChat.getInstance()
                .getChatHandler()
                .URL_PATTERN
                .matcher(message)
                .find();

        CompletableFuture<Boolean> urlBypassFuture;

        if (hasUrl) {
            urlBypassFuture = MineManiaChat.getInstance()
                    .getPermissionService()
                    .hasPermission(minecraftUuid, "chat.bypass.filter.url");
        } else {
            urlBypassFuture = CompletableFuture.completedFuture(true);
        }

        CompletableFuture<Boolean> disableBypassFuture = MineManiaChat.getInstance()
                .getPermissionService()
                .hasPermission(minecraftUuid, "chat.bypass.disable");

        urlBypassFuture
                .thenCombine(disableBypassFuture, (hasUrlBypass, hasDisableBypass) -> {
                    if (hasUrl && !hasUrlBypass) {
                        event.getMessage().delete().queue();

                        EmbedBuilder embed = new EmbedBuilder()
                                .setTitle("Message could not be delivered")
                                .setDescription("Try removing any URLs from your message.")
                                .setColor(Color.RED);

                        event.getMessage().replyEmbeds(embed.build()).queue();
                        return false;
                    }

                    if (!MineManiaChat.getInstance().getConfig().getBoolean("chat-enabled")) {
                        if (!hasDisableBypass) {
                            event.getMessage().delete().queue();

                            EmbedBuilder embed = new EmbedBuilder()
                                    .setTitle("Message could not be delivered")
                                    .setDescription("Chat is disabled on the server")
                                    .setColor(Color.RED);

                            event.getMessage().replyEmbeds(embed.build()).queue();
                            return false;
                        }
                    }

                    if (!MineManiaChat.getInstance().getConfig().getBoolean("discord-enabled")
                            || !MineManiaChat.getInstance().getConfig().getBoolean("chat-enabled")) {
                        if (!hasDisableBypass) {
                            event.getMessage().delete().queue();

                            EmbedBuilder embed = new EmbedBuilder()
                                    .setTitle("Message could not be delivered")
                                    .setDescription("The discord bridge is currently disabled")
                                    .setColor(Color.RED);

                            event.getMessage().replyEmbeds(embed.build()).queue();
                            return false;
                        }
                    }

                    return true;
                })
                .thenAccept(allowed -> {
                    if (!allowed) {
                        return;
                    }

                    formatMessage(member, minecraftUuid, minecraftUsername, message)
                            .thenAccept(chatReadyMessage -> {
                                for (Player p : MineManiaChat.getInstance().getProxyServer().getAllPlayers()) {
                                    p.sendMessage(
                                            LegacyComponentSerializer.legacyAmpersand().deserialize(
                                                    chatReadyMessage
                                            )
                                    );
                                }

                                MineManiaChat.getInstance().getLogger().info(
                                        LegacyComponentSerializer.legacyAmpersand()
                                                .deserialize(chatReadyMessage)
                                );
                            });
                })
                .exceptionally(error -> {
                    event.getMessage().delete().queue();

                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Message could not be delivered")
                            .setDescription("ERROR: could not check Minecraft permissions")
                            .setColor(Color.RED);

                    event.getMessage().replyEmbeds(embed.build()).queue();

                    error.printStackTrace();
                    return null;
                });
    }

    private CompletableFuture<String> formatMessage(
            Member member,
            UUID minecraftUuid,
            String minecraftUsername,
            String message
    ) {
        String formattedMessage = MineManiaChat.getInstance()
                .getDiscordHandler()
                .getDiscordConfig()
                .getString("discord-prefix");

        ConfigurationSection formatSection = MineManiaChat.getInstance()
                .getConfig()
                .getSection("format");

        CompletableFuture<FormatData> formatFuture = findFormatData(minecraftUuid, formatSection);

        CompletableFuture<Boolean> chatFormatFuture = MineManiaChat.getInstance()
                .getPermissionService()
                .hasPermission(minecraftUuid, "chat.format");

        return formatFuture.thenCombine(chatFormatFuture, (formatData, hasChatFormat) -> {
            String prefix = formatData.prefix();
            String postfix = formatData.postfix();

            if (!prefix.isBlank()) {
                prefix = prefix + " ";
            }

            if (!postfix.isBlank()) {
                postfix = " " + postfix;
            }

            String displayMessage = message;

            if (!hasChatFormat) {
                displayMessage = escapeLegacyFormatting(displayMessage);
            }

            return formattedMessage
                    + " "
                    + prefix
                    + "&f"
                    + minecraftUsername
                    + " &7: &f"
                    + displayMessage
                    + postfix;
        });
    }

    private CompletableFuture<FormatData> findFormatData(
            UUID minecraftUuid,
            ConfigurationSection formatSection
    ) {
        CompletableFuture<FormatData> future = CompletableFuture.completedFuture(
                new FormatData("", "")
        );

        for (String key : formatSection.getKeys()) {
            future = future.thenCompose(current -> {
                if (!current.prefix().isBlank() || !current.postfix().isBlank()) {
                    return CompletableFuture.completedFuture(current);
                }

                String permission = "chat." + key;

                return MineManiaChat.getInstance()
                        .getPermissionService()
                        .hasPermission(minecraftUuid, permission)
                        .thenApply(hasPermission -> {
                            if (!hasPermission) {
                                return current;
                            }

                            String prefix = formatSection
                                    .getSection(key)
                                    .getString("prefix", "");

                            String postfix = formatSection
                                    .getSection(key)
                                    .getString("postfix", "");

                            return new FormatData(prefix, postfix);
                        });
            });
        }

        return future;
    }

    private String escapeLegacyFormatting(String message) {
        return message.replace("&", "＆").replace("§", "§\u200B");
    }

    private record FormatData(String prefix, String postfix) {
    }
}