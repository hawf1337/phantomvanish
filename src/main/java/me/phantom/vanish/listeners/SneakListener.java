package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Listens for SHIFT (sneak) key presses and forwards them to the
 * {@link me.phantom.vanish.managers.SpectatorManager} for double-press
 * detection. Only the moment SHIFT is pressed down is relevant; releasing
 * it is ignored.
 */
public class SneakListener implements Listener {

    private final PhantomVanish plugin;

    public SneakListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            // We only care about the press, not the release.
            return;
        }

        Player player = event.getPlayer();
        plugin.getSpectatorManager().handleSneak(player);
    }
}
