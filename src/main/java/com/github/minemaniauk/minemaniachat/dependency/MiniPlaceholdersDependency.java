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

package com.github.minemaniauk.minemaniachat.dependency;

/**
 * Represents the mini-placeholders dependency.
 * Contains utility methods.
 */
public class MiniPlaceholdersDependency {

    /**
     * Used to check if the mini-placeholders dependency is enabled.
     *
     * @return True if enabled.
     */
    public static boolean isEnabled() {
        try {
            Class.forName("io.github.miniplaceholders.api.MiniPlaceholders");
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Used to get the dependency message.
     *
     * @return The requested string.
     */
    public static String getDependencyMessage() {
        return "Mini Placeholders : https://modrinth.com/plugin/miniplaceholders";
    }
}
