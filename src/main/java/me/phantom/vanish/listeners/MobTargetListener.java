package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Prevents mobs (and other entities) from targeting a vanished player,
 * so they don't get chased or attacked while invisible to everyone.
 * Controlled by {@code vanish.block-mob-targeting} in config.yml.
 */
public class MobTargetListener implements Listener {

    private final PhantomVanish plugin;

    public MobTargetListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!plugin.getConfigManager().isBlockMobTargeting()) {
            return;
        }
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        if (plugin.getVanishManager().isVanished(player)) {
            event.setCancelled(true);
        }
    }
}
