package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.GameMode;

/**
 * Listens for player interactions with entities (right-click on players).
 * Allows vanished or spectating players to open the inventory of targeted players.
 */
public class PlayerInteractListener implements Listener {

    private final PhantomVanish plugin;

    public PlayerInteractListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player clicker = event.getPlayer();
        
        // Check if clicker is vanished or in spectator mode
        if (!isVanishedOrSpectator(clicker)) {
            return;
        }

        // Check if the entity clicked is a player
        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player targetPlayer = (Player) event.getRightClicked();
        
        // Open the target player's inventory
        clicker.openInventory(targetPlayer.getInventory());
        
        // Cancel the event so normal interaction doesn't occur
        event.setCancelled(true);
    }

    /**
     * Checks if a player is vanished or in spectator mode.
     */
    private boolean isVanishedOrSpectator(Player player) {
        // Check if player is in spectator mode
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        // Check if player is vanished
        return plugin.getVanishManager().isVanished(player);
    }
}
