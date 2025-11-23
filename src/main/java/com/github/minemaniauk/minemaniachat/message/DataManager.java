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

package com.github.minemaniauk.minemaniachat.message;

import com.github.smuddgge.squishyconfiguration.ConfigurationFactory;
import com.github.smuddgge.squishyconfiguration.interfaces.Configuration;
import com.velocitypowered.api.proxy.Player;

import java.nio.file.Path;

public class DataManager {

    private final Path dataPath;
    private final Path playerDataPath;
    private final Configuration pmData;

    public DataManager (Path dataPath, Path playerDataPath) {
        this.dataPath = dataPath;
        this.playerDataPath = playerDataPath;

        this.pmData = ConfigurationFactory.YAML.create(this.dataPath.toFile(), "pmData");
    }

    public void globalEnablePms() {
        this.pmData.load();
        this.pmData.set("global-pms-enabled", true);
        this.pmData.save();
    }

    public void globalDisablePms() {
        this.pmData.load();
        this.pmData.set("global-pms-enabled", false);
        this.pmData.save();
    }

    public void enablePmsUser(Player player) {
        Configuration playerFile = ConfigurationFactory.YAML.create(playerDataPath.toFile(), player.getUniqueId().toString());
        playerFile.load();
        playerFile.set("pms-enabled", true);
        playerFile.save();
    }

    public void disablePmsUser(Player player) {
        Configuration playerFile = ConfigurationFactory.YAML.create(playerDataPath.toFile(), player.getUniqueId().toString());
        playerFile.load();
        playerFile.set("pms-enabled", false);
        playerFile.save();
    }

    public void enableSpy(Player player) {
        Configuration playerFile = ConfigurationFactory.YAML.create(playerDataPath.toFile(), player.getUniqueId().toString());
        playerFile.load();

        playerFile.set("is-spying", true);
        playerFile.save();
    }

    public void disableSpy(Player player) {
        Configuration playerFile = ConfigurationFactory.YAML.create(playerDataPath.toFile(), player.getUniqueId().toString());
        playerFile.load();

        playerFile.set("is-spying", false);
        playerFile.save();
    }

    public boolean isGlobalPmsEnabled() {
        pmData.load();
        return pmData.getBoolean("global-pms-enabled", true);
    }

    public boolean isPlayerPmFlagEnabled(Player player) {
        Configuration playerFile = ConfigurationFactory.YAML.create(playerDataPath.toFile(), player.getUniqueId().toString());
        playerFile.load();
        return playerFile.getBoolean("pms-enabled", true);
    }

    public boolean CanPlayerPm(Player player){
        this.pmData.load();
        if (!this.pmData.getBoolean("global-pms-enabled")){
            return false;
        }

        Configuration playerFile = ConfigurationFactory.YAML.create(playerDataPath.toFile(), player.getUniqueId().toString());
        playerFile.load();

        return playerFile.getBoolean("pms-enabled", true);
    }

    public boolean isSpying(Player player) {
        Configuration playerFile = ConfigurationFactory.YAML.create(playerDataPath.toFile(), player.getUniqueId().toString());
        playerFile.load();

        return playerFile.getBoolean("is-spying", false);
    }

}
