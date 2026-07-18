package me.phantom.vanish.managers;

import me.phantom.vanish.PhantomVanish;
import me.phantom.vanish.utils.MessageUtil;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the per-player item pickup blocking preference, toggled with
 * {@code /v item}.
 * <p>
 * When enabled for a player, they cannot pick up dropped items on the
 * ground while vanish is active. When disabled, item pickup behaves
 * normally regardless of vanish state. The preference itself has no effect
 * while the player is not vanished.
 */
public class ItemPickupManager {

    private final PhantomVanish plugin;
    private final Set<UUID> blockingEnabled = new HashSet<>();

    public ItemPickupManager(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    /**
     * Toggles the item-pickup-blocking preference for the given player and
     * sends feedback to chat (not the action bar, since this is a one-off
     * confirmation, not a persistent status) plus a sound cue.
     */
    public void toggle(Player player) {
        UUID uuid = player.getUniqueId();
        if (blockingEnabled.contains(uuid)) {
            blockingEnabled.remove(uuid);
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageItemPickupBlockDisabled());
        } else {
            blockingEnabled.add(uuid);
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageItemPickupBlockEnabled());
        }

        MessageUtil.playSound(player, plugin.getConfigManager().getSoundItemPickupToggle(),
                plugin.getConfigManager().getSoundVolume(), plugin.getConfigManager().getSoundPitch());
    }

    /**
     * @return whether the player has the item-pickup-blocking preference
     * enabled (independent of their current vanish state).
     */
    public boolean isBlockingEnabled(Player player) {
        return blockingEnabled.contains(player.getUniqueId());
    }

    /**
     * @return whether item pickup should currently be prevented for this
     * player: the preference must be enabled AND the player must currently
     * be vanished.
     */
    public boolean shouldBlockPickup(Player player) {
        return isBlockingEnabled(player) && plugin.getVanishManager().isVanished(player);
    }

    /**
     * Clears stored data for this player. Called on quit to avoid leaking
     * memory over time.
     */
    public void clearData(Player player) {
        blockingEnabled.remove(player.getUniqueId());
    }
}
