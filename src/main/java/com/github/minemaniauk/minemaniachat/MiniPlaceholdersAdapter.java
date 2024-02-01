package com.github.minemaniauk.minemaniachat;

import com.velocitypowered.api.proxy.Player;
import io.github.miniplaceholders.api.MiniPlaceholders;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;

public class MiniPlaceholdersAdapter {

    /**
     * Parse the mini placeholders.
     *
     * @param message The instance of the message to parse.
     * @param player  The instance of the player.
     * @return The parsed message.
     */
    public static Component parseMiniPlaceholders(String message, @Nullable Player player) {
        if (player != null) {
            return MiniMessage.miniMessage().deserialize(
                    message,
                    MiniPlaceholders.getAudienceGlobalPlaceholders(Audience.audience(player))
            );
        }

        return MiniMessage.miniMessage().deserialize(
                message,
                MiniPlaceholders.getGlobalPlaceholders()
        );
    }
}
