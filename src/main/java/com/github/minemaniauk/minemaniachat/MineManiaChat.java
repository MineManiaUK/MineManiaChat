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

import com.github.kerbity.kerb.packet.event.Priority;
import com.github.minemaniauk.api.MineManiaAPI;
import com.github.minemaniauk.api.MineManiaAPIContract;
import com.github.minemaniauk.api.kerb.event.player.PlayerChatEvent;
import com.github.minemaniauk.api.kerb.event.useraction.UserActionHasPermissionListEvent;
import com.github.minemaniauk.api.kerb.event.useraction.UserActionIsOnlineEvent;
import com.github.minemaniauk.api.kerb.event.useraction.UserActionIsVanishedEvent;
import com.github.minemaniauk.api.kerb.event.useraction.UserActionMessageEvent;
import com.github.minemaniauk.api.user.MineManiaUser;
import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;

@Plugin(
        id = "minemaniachat",
        name = "MineManiaChat",
        version = "1.0.0"
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
    public @NotNull PlayerChatEvent onChatEvent(@NotNull PlayerChatEvent event) {
        this.logger.info("<gray>[" + String.join(",", event.getServerWhiteList()) + "]"
                + event.getFormattedMessage()
        );
        return event;
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
     * Used to get the instance of the
     * mine mania chat plugin.
     *
     * @return The active mine mania chat instance.
     */
    public static @NotNull MineManiaChat getInstance() {
        return MineManiaChat.instance;
    }
}
