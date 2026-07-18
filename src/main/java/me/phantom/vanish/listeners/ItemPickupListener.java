package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

/**
 * Cancels item pickups for players who have the item pickup blocking
 * preference enabled while they are currently vanished. See
 * {@link me.phantom.vanish.managers.ItemPickupManager}.
 */
public class ItemPickupListener implements Listener {

    private final PhantomVanish plugin;

    public ItemPickupListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (plugin.getItemPickupManager().shouldBlockPickup(player)) {
            event.setCancelled(true);
        }
    }
}
