package me.phantom.vanish.listeners;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

/**
 * Keeps the internal double-shift spectator state in sync if a player's
 * gamemode is changed by something other than {@link me.phantom.vanish.managers.SpectatorManager}
 * itself (e.g. an admin command or another plugin), so future double-shift
 * presses behave correctly instead of relying on stale data.
 */
public class PlayerGameModeChangeListener implements Listener {

    private final PhantomVanish plugin;

    public PlayerGameModeChangeListener(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode newMode = event.getNewGameMode();

        if (plugin.getSpectatorManager().isInVanishSpectator(player) && newMode != GameMode.SPECTATOR) {
            plugin.getSpectatorManager().clearVanishSpectatorFlag(player);
        }
    }
}
