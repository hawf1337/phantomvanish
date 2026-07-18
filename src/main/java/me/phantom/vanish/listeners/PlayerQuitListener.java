package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player disconnects:
 * <ul>
 *     <li>Suppresses the quit message if the player is currently vanished.</li>
 *     <li>Cleans up runtime (online-only) vanish/spectator/item-pickup
 *     state. The persisted vanish flag itself is intentionally kept so the
 *     player is still vanished the next time they join.</li>
 * </ul>
 */
public class PlayerQuitListener implements Listener {

    private final PhantomVanish plugin;

    public PlayerQuitListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfigManager().isSilentJoinQuit() && plugin.getVanishManager().isVanished(player)) {
            event.quitMessage(null);
        }

        plugin.getVanishManager().handleQuit(player);
        plugin.getItemPickupManager().clearData(player);
    }
}
