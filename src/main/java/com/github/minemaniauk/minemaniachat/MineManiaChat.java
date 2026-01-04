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

import com.github.minemaniauk.minemaniachat.commands.Broadcast;
import com.github.minemaniauk.minemaniachat.commands.ChatClear;
import com.github.minemaniauk.minemaniachat.commands.JmSendCommand;
import com.github.minemaniauk.minemaniachat.commands.ServerMessage;
import com.github.minemaniauk.minemaniachat.message.DataManager;
import com.github.minemaniauk.minemaniachat.message.MessageHandler;
import com.github.minemaniauk.minemaniachat.message.commands.*;
import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

@Plugin(
        id = "minemaniachat",
        name = "MineManiaChat",
        version = "3.0.0"
)
public class MineManiaChat {

    private static @NotNull MineManiaChat instance;

    private final @NotNull ProxyServer server;
    private final @NotNull ComponentLogger logger;
    private final @NotNull Configuration configuration;
    private final @NotNull Configuration bannedWords;
    private @NotNull ChatHandler chatHandler;
    private final @NotNull MessageHandler messageHandler;
    private final @NotNull DataManager dataManager;
    private final @NotNull Path playerDataPath;
    private final @NotNull Path dataPath;

    @Inject
    public MineManiaChat(ProxyServer server, @DataDirectory final Path folder, ComponentLogger componentLogger) {
        MineManiaChat.instance = this;
        this.server = server;
        this.logger = componentLogger;
        this.dataPath = folder.resolve("data");
        this.playerDataPath = dataPath.resolve("player-data");

        // Set up the configuration file.
        this.configuration = ConfigurationFactory.YAML
                .create(folder.toFile(), "config")
                .setDefaultPath("config.yml");
        this.configuration.load();

        this.bannedWords = ConfigurationFactory.YAML
                .create(folder.toFile(), "bannedwords")
                .setDefaultPath("bannedwords.yml");
        this.bannedWords.load();



        // Create a new chat handler.
        this.chatHandler = new ChatHandler(this.configuration, this.bannedWords);
        this.messageHandler = new MessageHandler();
        this.dataManager = new DataManager(this.dataPath, this.playerDataPath);

        CommandManager cm = getProxyServer().getCommandManager();

        cm.register(cm.metaBuilder("clearchat").aliases("cc").build() , new ChatClear());
        cm.register(cm.metaBuilder("joinmessagesend").aliases("jmsend").build(), new JmSendCommand());
        cm.register(cm.metaBuilder("message").aliases("msg").build(), new Message());
        cm.register(cm.metaBuilder("enablepm").aliases("unmutepm").build(), new PmEnable());
        cm.register(cm.metaBuilder("disablepm").aliases("mutepm").build(), new PmDisable());
        cm.register(cm.metaBuilder("togglespy").aliases("spy").build(), new Spy());
        cm.register(cm.metaBuilder("servermessage").aliases("servermsg", "smsg").build(), new ServerMessage());
        cm.register(cm.metaBuilder("broadcast").build(), new Broadcast());
    }

    @Subscribe
    public void onPlayerJoinEvent(PlayerChooseInitialServerEvent event) {
        if (event.getPlayer().hasPermission("chat.joinmessage.disable")){
            sendJoinMessage(event.getPlayer(), true);
        }
        else { sendJoinMessage(event.getPlayer(), false);  }
    }

    @Subscribe
    public void onPlayerLeave(DisconnectEvent event) {
        if (event.getPlayer().hasPermission("chat.joinmessage.disable")){
            sendLeaveMessage(event.getPlayer(), true);
        }
        else { sendLeaveMessage(event.getPlayer(), false);  }
    }

    private void sendJoinMessage(Player player, boolean staffOnly){
        for (Player p : this.getProxyServer().getAllPlayers()) {
            if (!staffOnly){
                new User(p).sendMessage("&a+ &7" + player.getUsername());
            }
            else {
                if (p.hasPermission("chat.joinmessage.alert")){
                    new User(p).sendMessage("&a+ &7" + player.getUsername());
                }
            }

        }
    }
    private void sendLeaveMessage(Player player, boolean staffOnly){
        for (Player p : this.getProxyServer().getAllPlayers()) {
            if (!staffOnly){
                new User(p).sendMessage("&c- &7" + player.getUsername());
            }
            else {
                if (p.hasPermission("chat.joinmessage.alert")){
                    new User(p).sendMessage("&c- &7" + player.getUsername());
                }
            }

        }
    }

    public void reloadConfigs() {
        this.configuration.load();
        this.bannedWords.load();
        this.chatHandler = new ChatHandler(this.configuration, this.bannedWords);
    }

    /**
     * Used to get the instance of the Data Manager
     *
     * @return The instance of the Data Manager
     */
    public @NotNull DataManager getDataManager() { return this.dataManager; }

    /**
     * Used to get the instance of the message handler
     *
     * @return The instance of the message handler
     */
    public @NotNull MessageHandler getMessageHandler() { return this.messageHandler; }

    /**
     * Used to get the instance of the chat handler
     *
     * @return The instance of the chat handler
     */
    public @NotNull ChatHandler getChatHandler() { return this.chatHandler; }

    /**
     * Used to get the instance of the proxy server.
     *
     * @return The instance of the proxy server.
     */
    public @NotNull ProxyServer getProxyServer() {
        return this.server;
    }

    /**
     * Used to get the instance of the logger.
     *
     * @return The instance of the logger.
     */
    public @NotNull ComponentLogger getLogger() {
        return this.logger;
    }

    /**
     * Used to get instance of MineMania chat banned words list
     *
     * @return The instance of MineMania chat banned words list
     */
    public Configuration getBannedWords() { return this.bannedWords; }

    /**
     * Used to get instance of MineMania chat config
     *
     * @return The instance of MineMania chat config
     */

    public Configuration getConfig() { return this.configuration; }

    /**
     * Used to get the instance of the
     * mine mania chat plugin.
     *
     * @return The active mine mania chat instance.
     */
    public static @NotNull MineManiaChat getInstance() {
        return MineManiaChat.instance;
    }
}
