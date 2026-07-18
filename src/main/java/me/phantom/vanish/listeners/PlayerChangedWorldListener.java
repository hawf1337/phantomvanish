package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Re-applies correct vanish visibility whenever a player changes world,
 * since visibility state can be affected by world transitions on some
 * server implementations.
 */
public class PlayerChangedWorldListener implements Listener {

    private final PhantomVanish plugin;

    public PlayerChangedWorldListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // If this player is vanished, make sure they stay hidden from
        // everyone in their new world context.
        if (plugin.getVanishManager().isVanished(player)) {
            plugin.getVanishManager().reapplyVanishState(player);
        }

        // Make sure this player still can't see any vanished players
        // (or can, if they have the see-permission) in the new world.
        plugin.getVanishManager().updateVisibilityFor(player);
    }
}
