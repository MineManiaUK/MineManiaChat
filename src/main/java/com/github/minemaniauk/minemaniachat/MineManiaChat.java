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

import com.github.kerbity.kerb.client.listener.EventListener;
import com.github.kerbity.kerb.packet.event.Event;
import com.github.kerbity.kerb.packet.event.Priority;
import com.github.minemaniauk.api.MineManiaAPI;
import com.github.minemaniauk.api.MineManiaAPIContract;
import com.github.minemaniauk.api.database.collection.GameRoomCollection;
import com.github.minemaniauk.api.database.record.GameRoomRecord;
import com.github.minemaniauk.api.game.Arena;
import com.github.minemaniauk.api.kerb.event.GetOnlinePlayersRequest;
import com.github.minemaniauk.api.kerb.event.player.PlayerChatEvent;
import com.github.minemaniauk.api.kerb.event.useraction.*;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.implementation.YamlConfiguration;
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
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.myzelyam.api.vanish.VelocityVanishAPI;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

@Plugin(
        id = "minemaniachat",
        name = "MineManiaChat",
        version = "2.0.0 Preview"
)
public class MineManiaChat implements MineManiaAPIContract {

    private static @NotNull MineManiaChat instance;

    private final @NotNull ProxyServer server;
    private final @NotNull ComponentLogger logger;
    private final @NotNull Configuration configuration;
    private final @NotNull MineManiaAPI api;
    private final @NotNull ChatHandler chatHandler;

    @Inject
    public MineManiaChat(ProxyServer server, @DataDirectory final Path folder, ComponentLogger componentLogger) {
        MineManiaChat.instance = this;
        this.server = server;
        this.logger = componentLogger;



        // Set up the configuration file.
        this.configuration = ConfigurationFactory.YAML
                .create(folder.toFile(), "config")
                .setDefaultPath("config.yml");
        this.configuration.load();

        // Set up the mine mania api connection.
        this.api = MineManiaAPI.createAndSet(
                this.configuration,
                this
        );

        // Create a new chat handler.
        this.chatHandler = new ChatHandler(this.configuration);
        this.api.getKerbClient().registerListener(Priority.HIGH, this.chatHandler);

        this.api.getKerbClient().registerListener(Priority.LOW, new EventListener<GetOnlinePlayersRequest>() {
            @Override
            public @Nullable GetOnlinePlayersRequest onEvent(GetOnlinePlayersRequest event) {
                event.addOnlinePlayers(MineManiaChat.this.server.getAllPlayers().stream().map(Player::getUniqueId).toList());
                return event;
            }
        });

        CommandManager cm = getProxyServer().getCommandManager();

        var meta = cm.metaBuilder("clearchat")
                .aliases("cc")
                .build();

        cm.register(meta , new ChatClear());

    }

    @Override
    public @NotNull MineManiaUser getUser(@NotNull UUID uuid) {
        return new MineManiaUser(uuid, this.server.getPlayer(uuid).orElseThrow().getUsername());
    }

    @Override
    public @NotNull MineManiaUser getUser(@NotNull String name) {
        return new MineManiaUser(this.server.getPlayer(name).orElseThrow().getUniqueId(), name);
    }

    @Override
    public @Nullable UserActionHasPermissionListEvent onHasPermission(@NotNull UserActionHasPermissionListEvent event) {
        return null;
    }

    @Override
    public @Nullable UserActionIsOnlineEvent onIsOnline(@NotNull UserActionIsOnlineEvent event) {
        return null;
    }

    @Override
    public @Nullable UserActionIsVanishedEvent onIsVanished(@NotNull UserActionIsVanishedEvent event) {
        return null;
    }

    @Override
    public @Nullable UserActionMessageEvent onMessage(@NotNull UserActionMessageEvent event) {
        return null;
    }

    @Override
    public @Nullable UserActionTeleportEvent onTeleport(@NotNull UserActionTeleportEvent event) {
        this.getPlayer(event.getUser()).ifPresent(user -> {
            RegisteredServer registeredServer = event.getLocation().getLocation(new VelocityLocationConverter());
            user.createConnectionRequest(registeredServer).connect();
        });
        return (UserActionTeleportEvent) event.setComplete(true);
    }

    @Override
    public @NotNull PlayerChatEvent onChatEvent(@NotNull PlayerChatEvent event) {
        this.logger.info("<gray>[" + String.join(",", event.getServerWhiteList()) + "]"
                + event.getFormattedMessage()
        );
        return event;
    }

    @Subscribe
    public void onPlayerJoinEvent(PlayerChooseInitialServerEvent event) {
        for (Player player : this.getProxyServer().getAllPlayers()) {
            new User(player).sendMessage("&a+ &7" + event.getPlayer().getUsername());
        }
    }

    @Subscribe
    public void onPlayerLeave(DisconnectEvent event) {
        for (Player player : this.getProxyServer().getAllPlayers()) {
            new User(player).sendMessage("&c- &7" + event.getPlayer().getUsername());
        }
    }

    /**
     * Used to get the instance of a player from a mine mania user.
     *
     * @param user The instance of the user.
     * @return The optional player instance.
     * It will be empty if the player is not online on this server.
     */
    public @NotNull Optional<Player> getPlayer(@NotNull MineManiaUser user) {
        return MineManiaChat.getInstance().getProxyServer().getPlayer(user.getUniqueId());
    }

    /**
     * Used to get the instance of a mine mania user from a player.
     *
     * @param player The instance of the player.
     * @return The requested mine mania user instance.
     */
    public @NotNull MineManiaUser getUser(@NotNull Player player) {
        return new MineManiaUser(player.getUniqueId(), player.getUsername());
    }


    /**
     * Used to get a filtered list of players.
     * <ul>
     *     <li>Filters players with the permission.</li>
     * </ul>
     *
     * @param permission      The permission to filter.
     * @param permissions     The possible permissions to filter.
     *                        include vanished players.
     * @return List of filtered players.
     */
    public @NotNull List<User> getFilteredPlayers(String permission, List<String> permissions) {
        List<User> players = new ArrayList<>();

        for (Player player : this.server.getAllPlayers()) {
            User user = new User(player);

            // If the player has the permission node
            if (!user.hasPermission(permission)) continue;

            // Check if it's there the highest permission
            if (!Objects.equals(user.getHighestPermission(permissions), permission)) continue;


            players.add(user);
        }

        return players;
    }

    /**
     * Used to get a filtered list of players on a server.
     * <ul>
     *     <li>Filters players with the permission.</li>
     * </ul>
     *
     * @param server          The instance of a server.
     * @param permission      The permission to filter.
     *                        include vanished players.
     * @return List of filtered players.
     */
    public @NotNull List<User> getFilteredPlayers(RegisteredServer server, String permission) {
        List<User> players = new ArrayList<>();

        for (Player player : server.getPlayersConnected()) {
            User user = new User(player);

            // If the player has the permission node
            if (!user.hasPermission(permission)) continue;

            players.add(user);
        }

        return players;
    }

    /**
     * Used to get a list of registered server names.
     *
     * @return The list of server names.
     */
    public @NotNull List<String> getServerNames() {
        List<String> servers = new ArrayList<>();

        for (RegisteredServer server : MineManiaChat.getInstance().getProxyServer().getAllServers()) {
            servers.add(server.getServerInfo().getName());
        }

        return servers;
    }

    /**
     * Used to get a random player from a server.
     *
     * @param server The instance of a server.
     * @return A random player.
     */
    public @Nullable User getRandomUser(RegisteredServer server) {
        for (Player player : server.getPlayersConnected()) {
            return new User(player);
        }
        return null;
    }

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
