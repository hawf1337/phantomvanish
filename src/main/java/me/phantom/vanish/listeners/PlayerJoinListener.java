package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Handles player joins:
 * <ul>
 *     <li>Suppresses the join message if the joining player is (still)
 *     vanished, e.g. because they disconnected while vanished.</li>
 *     <li>Applies correct visibility in both directions: hides already
 *     vanished players from the newcomer, and re-hides the newcomer from
 *     everyone else if they themselves are vanished.</li>
 * </ul>
 */
public class PlayerJoinListener implements Listener {

    private final PhantomVanish plugin;

    public PlayerJoinListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfigManager().isSilentJoinQuit() && plugin.getVanishManager().isVanished(player)) {
            event.joinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinMonitor(PlayerJoinEvent event) {
        plugin.getVanishManager().handleJoin(event.getPlayer());
    }
}
