package me.phantom.vanish.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Small static helper for sending Adventure action bar messages and playing
 * sounds, keeping repetitive boilerplate out of the manager classes.
 */
public final class MessageUtil {

    private MessageUtil() {
        // Utility class, no instances.
    }

    /**
     * Sends a normal chat message to the given player, translating legacy
     * '&' color codes into an Adventure {@link Component}. Used for things
     * like the help command, where a persistent chat line is wanted instead
     * of a fading action bar message.
     */
    public static void sendMessage(Player player, String legacyText) {
        if (legacyText == null || legacyText.isEmpty()) {
            return;
        }
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(legacyText);
        player.sendMessage(component);
    }

    /**
     * Sends an action bar message to the given player, translating legacy
     * '&' color codes into an Adventure {@link Component}.
     */
    public static void sendActionBar(Player player, String legacyText) {
        if (legacyText == null || legacyText.isEmpty()) {
            return;
        }
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(legacyText);
        player.sendActionBar(component);
    }

    /**
     * Plays a sound for the given player at their current location, audible
     * only to that player (client-side positioned).
     */
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        if (sound == null) {
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
