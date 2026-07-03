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

import com.github.minemaniauk.minemaniachat.commands.*;
import com.github.minemaniauk.minemaniachat.discord.DiscordManager;
import com.github.minemaniauk.minemaniachat.discord.commands.minecraft.*;
import com.github.minemaniauk.minemaniachat.discord.link.LinkManager;
import com.github.minemaniauk.minemaniachat.discord.link.LinkStorage;
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
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.sbcomputerteh.chatwatch.cwvelocity.CWVelocity;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.luckperms.api.LuckPermsProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Plugin(
        id = "minemaniachat",
        name = "MineManiaChat",
        version = "3.4.4"
)
public class MineManiaChat {

    private static @NotNull MineManiaChat instance;

    private PermissionService permissionService;
    private final @NotNull ProxyServer server;
    private final @NotNull ComponentLogger logger;
    private final @NotNull Configuration configuration;
    private  final @NotNull Configuration discordConfig;
    private final @NotNull Configuration bannedWords;
    private final @NotNull Configuration linksConfiguration;
    private @NotNull ChatHandler chatHandler;
    private DataBaseController dbController;
    private final @NotNull MessageHandler messageHandler;
    private final @NotNull DataManager dataManager;
    private final @NotNull Path playerDataPath;
    private final @NotNull Path dataPath;
    private CWVelocity cw;
    private DiscordManager discordManager;
    private LinkStorage linkStorage;
    private LinkManager linkManager;

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

        // Set up the discord config file
        this.discordConfig = ConfigurationFactory.YAML
                .create(folder.toFile(), "discord")
                .setDefaultPath("discord.yml");
        this.discordConfig.load();

        // Set up the banned words config file
        this.bannedWords = ConfigurationFactory.YAML
                .create(folder.toFile(), "bannedwords")
                .setDefaultPath("bannedwords.yml");
        this.bannedWords.load();

        // links.yml
        this.linksConfiguration = ConfigurationFactory.YAML
                .create(folder.toFile(), "links");

        this.linksConfiguration.load();

        this.server.getPluginManager().getPlugin("cwvelocity")
                .flatMap(pluginContainer -> pluginContainer.getInstance())
                .ifPresentOrElse(
                        instance -> {
                            if (instance instanceof CWVelocity cwVelocity) {
                                this.cw = cwVelocity;
                            } else {
                                logger.warn("cwvelocity plugin instance is not a CWVelocity instance");
                            }
                        },
                        () -> logger.warn("Could not find cwvelocity installed")
                );

        // Create a new chat handler.
        this.chatHandler = new ChatHandler(this.configuration, this.bannedWords);
        this.messageHandler = new MessageHandler();
        this.dataManager = new DataManager(this.dataPath, this.playerDataPath);
        if (configuration.getBoolean("database.enabled")){
            this.dbController = new DataBaseController(this.configuration);
        }

        CommandManager cm = getProxyServer().getCommandManager();

        if (discordConfig.getBoolean("enabled")) {
            this.linkStorage = new LinkStorage();
            this.linkManager = new LinkManager(linkStorage);
            this.discordManager = new DiscordManager(discordConfig);
            cm.register(cm.metaBuilder("mmchatdiscord").build(), new DiscordCommand());
            cm.register(cm.metaBuilder("mmchatsetdiscordPresence").build(), new DiscordPresence());
            cm.register(cm.metaBuilder("discordlink").aliases("link").build(), new LinkCommand(this.linkManager));
            cm.register(cm.metaBuilder("discordunlink").aliases("unlink").build(), new UnlinkCommand(this.linkStorage));
            cm.register(cm.metaBuilder("discordadminunlink").aliases("aunlink").build(), new AdminUnlinkCommand(this.linkStorage));
            cm.register(cm.metaBuilder("discordadminlink").aliases("alink").build(), new AdminLinkCommand(this.linkManager));
        }

        cm.register(cm.metaBuilder("chatenable").build(), new ChatEnable());
        cm.register(cm.metaBuilder("chatdisable").build(), new ChatDisable());
        cm.register(cm.metaBuilder("mmchatbannedwords").build(), new BannedWords());
        cm.register(cm.metaBuilder("mmchatreload").build(), new Reload());
        cm.register(cm.metaBuilder("clearchat").aliases("cc").build() , new ChatClear());
        cm.register(cm.metaBuilder("joinmessagesend").aliases("jmsend").build(), new JmSendCommand());
        cm.register(cm.metaBuilder("message").aliases("msg").build(), new Message());
        cm.register(cm.metaBuilder("enablepm").aliases("unmutepm").build(), new PmEnable());
        cm.register(cm.metaBuilder("disablepm").aliases("mutepm").build(), new PmDisable());
        cm.register(cm.metaBuilder("togglespy").aliases("spy").build(), new Spy());
        cm.register(cm.metaBuilder("servermessage").aliases("servermsg", "smsg").build(), new ServerMessage());
        cm.register(cm.metaBuilder("broadcast").build(), new Broadcast());
        cm.register(cm.metaBuilder("broadcastserver").build(), new BroadcastServer());
        cm.register(cm.metaBuilder("togglechatalerts").aliases("chatalerts").build(), new Alerts());
        cm.register(cm.metaBuilder("chat").aliases("c", "talk").build(), new Chat());
        cm.register(cm.metaBuilder("mmchatspamcooldown").build(), new SpamCooldown());
    }

    @Subscribe
    public void ProxyInitEvent(ProxyInitializeEvent event) {
        this.server.getEventManager().register(this, this.chatHandler);
        this.permissionService = new PermissionService(LuckPermsProvider.get());
    }

    @Subscribe
    public void OnShutdown(ProxyShutdownEvent event){
        dbController.close();
    }

    @Subscribe
    public void onPlayerJoinEvent(PlayerChooseInitialServerEvent event) {
        if (event.getPlayer().hasPermission("chat.joinmessage.disable")){
            sendJoinMessage(event.getPlayer(), true);
        }
        else { sendJoinMessage(event.getPlayer(), false);  }
        linkStorage.updateMinecraftUsername(event.getPlayer().getUniqueId(), event.getPlayer().getUsername());
    }

    @Subscribe
    public void onPlayerLeave(DisconnectEvent event) {
        if (event.getPlayer().hasPermission("chat.joinmessage.disable")){
            sendLeaveMessage(event.getPlayer(), true);
        }
        else { sendLeaveMessage(event.getPlayer(), false);  }
    }

    private void sendJoinMessage(Player player, boolean staffOnly) {
        if (!staffOnly) {
            getDiscordManager().sendJoinMessage(player);
        }

        for (Player p : this.getProxyServer().getAllPlayers()) {
            if (!staffOnly) {
                new User(p).sendMessage("&a+ &7" + player.getUsername());
            } else {
                if (p.hasPermission("chat.joinmessage.alert")) {
                    new User(p).sendMessage("&a+ &7" + player.getUsername());
                }
            }
        }
    }

    private void sendLeaveMessage(Player player, boolean staffOnly) {
        if (!staffOnly) {
            getDiscordManager().sendLeaveMessage(player);
        }

        for (Player p : this.getProxyServer().getAllPlayers()) {
            if (!staffOnly) {
                new User(p).sendMessage("&c- &7" + player.getUsername());
            } else {
                if (p.hasPermission("chat.joinmessage.alert")) {
                    new User(p).sendMessage("&c- &7" + player.getUsername());
                }
            }
        }
    }

    public void reloadBannedWords() {
        this.server.getEventManager().unregisterListener(this, this.chatHandler);
        this.bannedWords.load();
        this.chatHandler = new ChatHandler(this.configuration, this.bannedWords);
        this.server.getEventManager().register(this, this.chatHandler);
    }

    public void reloadConfigs() {
        this.server.getEventManager().unregisterListener(this, this.chatHandler);
        this.configuration.load();
        this.bannedWords.load();
        this.discordConfig.load();
        this.chatHandler = new ChatHandler(this.configuration, this.bannedWords);
        if (configuration.getBoolean("database.enabled")){
            this.dbController = new DataBaseController(this.configuration);
        }
        if (discordConfig.getBoolean("enabled")) {
            this.discordManager.discordConfig = this.discordConfig;
        }
        this.server.getEventManager().register(this, this.chatHandler);
    }

    public CWVelocity getCw(){
        return this.cw;
    }

    public Configuration getDiscordConfig() { return discordConfig; }

    public Configuration getLinksConfig() {
        return linksConfiguration;
    }

    public LinkManager getLinkManager() {
        return linkManager;
    }

    public PermissionService getPermissionService() { return permissionService; }

    /**
     * Used to get the instance of the Discord Handler
     *
     * @return The instance of the Discord Handler
     */
    public @NotNull DiscordManager getDiscordManager() { return this.discordManager; }

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
     * Used to get the instance of the DataBase Controller
     *
     * @return The instance of the DataBase Controller
     */
    public @NotNull DataBaseController getDbController() { return this.dbController; }

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
