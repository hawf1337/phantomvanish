package me.phantom.vanish.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

/**
 * Small static helper for sending Adventure action bar messages, broadcasts,
 * and playing sounds, keeping repetitive boilerplate out of the manager classes.
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

    /**
     * Broadcasts a stylized message to all players with the vanish.broadcast permission.
     */
    public static void broadcastVanishMessage(String playerName, boolean vanished) {
        Component separator = Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY);
        Component message;

        if (vanished) {
            message = Component.text()
                    .append(Component.text("👤 ").color(NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text(playerName).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                    .append(Component.text(" has entered vanish").color(NamedTextColor.LIGHT_PURPLE))
                    .build();
        } else {
            message = Component.text()
                    .append(Component.text("👤 ").color(NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text(playerName).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                    .append(Component.text(" has left vanish").color(NamedTextColor.LIGHT_PURPLE))
                    .build();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("phantomvanish.broadcast")) {
                player.sendMessage(separator);
                player.sendMessage(message);
                player.sendMessage(separator);
            }
        }
    }

    /**
     * Broadcasts a stylized message when an admin changes another player's vanish state.
     */
    public static void broadcastVanishActionMessage(String adminName, String targetName, boolean vanished) {
        Component separator = Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY);
        Component message;

        if (vanished) {
            message = Component.text()
                    .append(Component.text("⚡ ").color(NamedTextColor.GOLD))
                    .append(Component.text(adminName).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                    .append(Component.text(" set ").color(NamedTextColor.GOLD))
                    .append(Component.text(targetName).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                    .append(Component.text(" to vanish").color(NamedTextColor.GOLD))
                    .build();
        } else {
            message = Component.text()
                    .append(Component.text("⚡ ").color(NamedTextColor.GOLD))
                    .append(Component.text(adminName).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                    .append(Component.text(" set ").color(NamedTextColor.GOLD))
                    .append(Component.text(targetName).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                    .append(Component.text(" to visible").color(NamedTextColor.GOLD))
                    .build();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("phantomvanish.broadcast")) {
                player.sendMessage(separator);
                player.sendMessage(message);
                player.sendMessage(separator);
            }
        }
    }
}
