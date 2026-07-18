package me.phantom.vanish.managers;

import me.phantom.vanish.PhantomVanish;
import me.phantom.vanish.storage.VanishStorage;
import me.phantom.vanish.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the vanish state of players and controls their visibility
 * towards other players on the server.
 * <p>
 * A vanished player is hidden from everyone who lacks the
 * {@code phantomvanish.vanish.see} permission, including players who join
 * after the vanish was activated. No potion effects or particles are used —
 * visibility is controlled purely through
 * {@link Player#hidePlayer(org.bukkit.plugin.Plugin, Player)} /
 * {@link Player#showPlayer(org.bukkit.plugin.Plugin, Player)}.
 * <p>
 * Vanish state is persisted through the configured {@link VanishStorage}
 * backend (local YAML file by default, or MySQL) so a player who
 * disconnects while vanished (or the server restarts) is still vanished
 * the next time they join.
 */
public class VanishManager {

    private final PhantomVanish plugin;
    private final VanishStorage storage;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    /** Repeating task that keeps the "Vanish Enabled" action bar visible. */
    private final Map<UUID, BukkitTask> actionBarTasks = new HashMap<>();

    public VanishManager(PhantomVanish plugin, VanishStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.vanishedPlayers.addAll(storage.loadVanishedPlayers());
    }

    /**
     * Toggles vanish state for the given player.
     */
    public void toggleVanish(Player player) {
        if (isVanished(player)) {
            disableVanish(player);
        } else {
            enableVanish(player);
        }
    }

    /**
     * Enables vanish for the given player: hides them from everyone without
     * the see-permission and sends feedback (action bar + sound).
     */
    public void enableVanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());
        hideFromAll(player);
        persistAdd(player.getUniqueId());

        MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessageVanishEnabled());
        MessageUtil.playSound(player, plugin.getConfigManager().getSoundVanishEnable(),
                plugin.getConfigManager().getSoundVolume(), plugin.getConfigManager().getSoundPitch());

        startActionBarTask(player);
    }

    /**
     * Disables vanish for the given player: restores their visibility to
     * everyone and, if they were in the double-shift spectator mode,
     * restores their previous gamemode first.
     */
    public void disableVanish(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
        persistRemove(player.getUniqueId());
        stopActionBarTask(player);

        // If the player entered spectator via double-shift while vanished,
        // restore their original gamemode before becoming visible again.
        if (plugin.getSpectatorManager().isInVanishSpectator(player)) {
            plugin.getSpectatorManager().exitSpectator(player, false);
        }

        showToAll(player);

        MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessageVanishDisabled());
        MessageUtil.playSound(player, plugin.getConfigManager().getSoundVanishDisable(),
                plugin.getConfigManager().getSoundVolume(), plugin.getConfigManager().getSoundPitch());
    }

    /**
     * Starts a repeating task that keeps re-sending the "Vanish Enabled"
     * action bar to the player, since action bar messages fade out after a
     * few seconds if not refreshed.
     */
    private void startActionBarTask(Player player) {
        stopActionBarTask(player);

        long interval = plugin.getConfigManager().getVanishActionBarInterval();
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || !isVanished(player)) {
                stopActionBarTask(player);
                return;
            }
            MessageUtil.sendActionBar(player, plugin.getConfigManager().getMessageVanishEnabled());
        }, interval, interval);

        actionBarTasks.put(player.getUniqueId(), task);
    }

    /**
     * Cancels and removes the persistent action bar task for a player, if
     * one is currently running.
     */
    private void stopActionBarTask(Player player) {
        BukkitTask task = actionBarTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }

    /**
     * @return an unmodifiable view of every currently vanished player's UUID
     * (includes offline players who were vanished when they disconnected).
     */
    public Set<UUID> getVanishedPlayers() {
        return Collections.unmodifiableSet(vanishedPlayers);
    }

    /**
     * @return the display names of every vanished player who is currently
     * online, used by the {@code /v list} command.
     */
    public List<String> getVanishedOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (UUID uuid : vanishedPlayers) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                names.add(player.getName());
            }
        }
        return names;
    }

    /**
     * Hides the vanished player from every online player that does not have
     * permission to see vanished players.
     */
    private void hideFromAll(Player vanished) {
        String seePermission = plugin.getConfigManager().getPermissionVanishSee();
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(vanished)) {
                continue;
            }
            if (!online.hasPermission(seePermission)) {
                online.hidePlayer(plugin, vanished);
            }
        }
    }

    /**
     * Shows the previously vanished player to everyone online.
     */
    private void showToAll(Player vanished) {
        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (online.equals(vanished)) {
                continue;
            }
            online.showPlayer(plugin, vanished);
        }
    }

    /**
     * Applies correct visibility of all vanished players towards a single
     * viewer. Used when a player joins the server or changes world, so that
     * newly joined or relocated players still cannot see vanished players.
     */
    public void updateVisibilityFor(Player viewer) {
        boolean canSeeVanished = viewer.hasPermission(plugin.getConfigManager().getPermissionVanishSee());
        for (UUID uuid : vanishedPlayers) {
            Player vanishedPlayer = plugin.getServer().getPlayer(uuid);
            if (vanishedPlayer == null || vanishedPlayer.equals(viewer)) {
                continue;
            }
            if (canSeeVanished) {
                viewer.showPlayer(plugin, vanishedPlayer);
            } else {
                viewer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    /**
     * Re-applies a vanished player's invisibility towards every other online
     * player. Useful after events such as world changes, where visibility
     * state can otherwise be reset by the server.
     */
    public void reapplyVanishState(Player vanishedPlayer) {
        if (!isVanished(vanishedPlayer)) {
            return;
        }
        hideFromAll(vanishedPlayer);
    }

    /**
     * Called when a player joins the server. Handles two things:
     * <ol>
     *     <li>Hides every already-vanished (other) player from this new
     *     viewer, unless they have the see-permission.</li>
     *     <li>If this player was themselves vanished before disconnecting
     *     (or before a server restart), restores their vanish visibility
     *     and reminds them via chat that they are still vanished.</li>
     * </ol>
     */
    public void handleJoin(Player player) {
        updateVisibilityFor(player);

        if (isVanished(player)) {
            hideFromAll(player);
            startActionBarTask(player);
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageVanishReminder());
        }
    }

    /**
     * Cleans up runtime (online-only) vanish state when a player leaves the
     * server. Deliberately does NOT remove the player from
     * {@link #vanishedPlayers} — their vanish state is persisted and will be
     * restored automatically the next time they join.
     */
    public void handleQuit(Player player) {
        stopActionBarTask(player);
        plugin.getSpectatorManager().clearData(player);
    }

    /**
     * Restores visibility for every online vanished player when the plugin
     * is disabled, so nobody is left permanently invisible after a reload.
     * The persisted vanish list itself is left untouched, so vanish state
     * survives a server restart.
     */
    public void restoreAllOnDisable() {
        for (UUID uuid : new HashSet<>(vanishedPlayers)) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                stopActionBarTask(player);
                showToAll(player);
            }
        }
        actionBarTasks.values().forEach(BukkitTask::cancel);
        actionBarTasks.clear();
    }

    /**
     * Persists that a player became vanished. Runs off the main thread so a
     * slow disk write or MySQL round-trip never causes a server hiccup.
     */
    private void persistAdd(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> storage.addVanishedPlayer(uuid));
    }

    /**
     * Persists that a player is no longer vanished. Runs off the main
     * thread for the same reason as {@link #persistAdd(UUID)}.
     */
    private void persistRemove(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> storage.removeVanishedPlayer(uuid));
    }
}
