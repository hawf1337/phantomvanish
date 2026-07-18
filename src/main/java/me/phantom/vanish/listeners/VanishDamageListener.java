package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Cancels any damage a vanished player would take (fall, fire, mobs,
 * environment, etc.), since a vanished player is effectively "not really
 * there". Controlled by {@code vanish.block-damage} in config.yml.
 */
public class VanishDamageListener implements Listener {

    private final PhantomVanish plugin;

    public VanishDamageListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.getConfigManager().isBlockDamage()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (plugin.getVanishManager().isVanished(player)) {
            event.setCancelled(true);
        }
    }
}
