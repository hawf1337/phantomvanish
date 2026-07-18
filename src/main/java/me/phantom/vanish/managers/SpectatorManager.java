package me.phantom.vanish.managers;

import me.phantom.vanish.PhantomVanish;
import me.phantom.vanish.utils.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the double-shift-triggered spectator mode that is only available
 * while a player is vanished.
 * <p>
 * Detection works by recording the timestamp of the first SHIFT press and
 * comparing it against the second press. If both presses happen within the
 * configured threshold (default 400ms), spectator mode is toggled.
 * <p>
 * When leaving spectator, flight is restored so staff never fall out of
 * the sky. By default ({@code spectator.auto-fly-on-exit: true}) this
 * always grants flight back regardless of whether the player had /fly
 * enabled before entering spectator, so nobody has to type /fly manually
 * after returning from spectator. Setting that option to {@code false}
 * instead restores exactly the flight state the player had beforehand.
 */
public class SpectatorManager {

    private final PhantomVanish plugin;

    /** Gamemode a player had right before entering vanish-spectator. */
    private final Map<UUID, GameMode> savedGameModes = new HashMap<>();

    /** Whether the player was allowed to fly right before entering vanish-spectator. */
    private final Map<UUID, Boolean> savedAllowFlight = new HashMap<>();

    /** Whether the player was actively flying right before entering vanish-spectator. */
    private final Map<UUID, Boolean> savedFlying = new HashMap<>();

    /** Players currently in spectator mode because of the double-shift trick. */
    private final Set<UUID> inVanishSpectator = new HashSet<>();

    /** Timestamp (ms) of the last recorded SHIFT press, per player. */
    private final Map<UUID, Long> lastSneakTimestamps = new HashMap<>();

    public SpectatorManager(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles a sneak (SHIFT) key press for the given player and triggers
     * the spectator toggle if a double press is detected within the
     * configured threshold. Does nothing if the spectator feature is
     * disabled in config or if the player is not currently vanished.
     */
    public void handleSneak(Player player) {
        if (!plugin.getConfigManager().isSpectatorEnabled()) {
            return;
        }
        if (!plugin.getVanishManager().isVanished(player)) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long threshold = plugin.getConfigManager().getDoubleShiftTime();

        Long lastPress = lastSneakTimestamps.get(uuid);
        if (lastPress != null && (now - lastPress) <= threshold) {
            // Double press detected within the threshold window.
            lastSneakTimestamps.remove(uuid);
            toggleSpectator(player);
        } else {
            lastSneakTimestamps.put(uuid, now);
        }
    }

    private void toggleSpectator(Player player) {
        if (inVanishSpectator.contains(player.getUniqueId())) {
            exitSpectator(player, true);
        } else {
            enterSpectator(player);
        }
    }

    private void enterSpectator(Player player) {
        UUID uuid = player.getUniqueId();
        savedGameModes.put(uuid, player.getGameMode());
        // Spectator mode always grants flight; remember the player's real
        // flight state so it can be restored exactly as it was afterwards
        // if auto-fly-on-exit is turned off (e.g. if they had /fly enabled
        // or were in Creative flying).
        savedAllowFlight.put(uuid, player.getAllowFlight());
        savedFlying.put(uuid, player.isFlying());

        inVanishSpectator.add(uuid);
        player.setGameMode(GameMode.SPECTATOR);

        MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessageSpectatorEnabled());
        MessageUtil.playSound(player, plugin.getConfigManager().getSoundSpectatorEnter(),
                plugin.getConfigManager().getSoundVolume(), plugin.getConfigManager().getSoundPitch());
    }

    /**
     * Exits the vanish-spectator mode and restores the previously saved
     * gamemode.
     *
     * @param player       the player to restore
     * @param notifyPlayer whether to send the action bar message and play
     *                     the exit sound (set to {@code false} when this is
     *                     triggered indirectly, e.g. by disabling vanish,
     *                     since that flow sends its own feedback)
     */
    public void exitSpectator(Player player, boolean notifyPlayer) {
        UUID uuid = player.getUniqueId();
        GameMode previous = savedGameModes.remove(uuid);
        Boolean previousAllowFlight = savedAllowFlight.remove(uuid);
        Boolean previousFlying = savedFlying.remove(uuid);
        inVanishSpectator.remove(uuid);

        if (previous == null) {
            previous = GameMode.SURVIVAL;
        }
        player.setGameMode(previous);

        // setGameMode() resets allowFlight/flying based on the new mode
        // (e.g. Survival always disables flight), so flight needs to be
        // re-applied on top of that afterwards.
        if (plugin.getConfigManager().isAutoFlyOnSpectatorExit()) {
            // Always hand flight back, whether or not the player had /fly
            // enabled before entering spectator, so they never fall and
            // never have to type /fly manually after coming back.
            player.setAllowFlight(true);
            player.setFlying(true);
        } else if (previousAllowFlight != null && previousAllowFlight) {
            // Legacy behaviour: only restore flight if the player actually
            // had it before entering spectator.
            player.setAllowFlight(true);
            if (previousFlying != null && previousFlying) {
                player.setFlying(true);
            }
        }

        if (notifyPlayer) {
            MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessageSpectatorDisabled());
            MessageUtil.playSound(player, plugin.getConfigManager().getSoundSpectatorExit(),
                    plugin.getConfigManager().getSoundVolume(), plugin.getConfigManager().getSoundPitch());
        }
    }

    public boolean isInVanishSpectator(Player player) {
        return inVanishSpectator.contains(player.getUniqueId());
    }

    /**
     * Clears the internal vanish-spectator flag and saved gamemode without
     * changing the player's current gamemode. Used when the gamemode was
     * already changed externally (e.g. by another plugin or command), so
     * our tracked state does not go stale.
     */
    public void clearVanishSpectatorFlag(Player player) {
        UUID uuid = player.getUniqueId();
        inVanishSpectator.remove(uuid);
        savedGameModes.remove(uuid);
        savedAllowFlight.remove(uuid);
        savedFlying.remove(uuid);
    }

    /**
     * Clears all stored data related to this player. Called on quit to
     * avoid leaking memory over time.
     */
    public void clearData(Player player) {
        UUID uuid = player.getUniqueId();
        savedGameModes.remove(uuid);
        savedAllowFlight.remove(uuid);
        savedFlying.remove(uuid);
        inVanishSpectator.remove(uuid);
        lastSneakTimestamps.remove(uuid);
    }
}
