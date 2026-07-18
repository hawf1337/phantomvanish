package me.phantom.vanish;

import me.phantom.vanish.commands.VanishCommand;
import me.phantom.vanish.listeners.ItemPickupListener;
import me.phantom.vanish.listeners.MobTargetListener;
import me.phantom.vanish.listeners.PlayerChangedWorldListener;
import me.phantom.vanish.listeners.PlayerGameModeChangeListener;
import me.phantom.vanish.listeners.PlayerJoinListener;
import me.phantom.vanish.listeners.PlayerQuitListener;
import me.phantom.vanish.listeners.SneakListener;
import me.phantom.vanish.listeners.VanishDamageListener;
import me.phantom.vanish.managers.ItemPickupManager;
import me.phantom.vanish.managers.SpectatorManager;
import me.phantom.vanish.managers.VanishManager;
import me.phantom.vanish.storage.MySQLVanishStorage;
import me.phantom.vanish.storage.StorageType;
import me.phantom.vanish.storage.VanishStorage;
import me.phantom.vanish.storage.YamlVanishStorage;
import me.phantom.vanish.utils.ConfigManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main entry point of the PhantomVanish plugin.
 * <p>
 * Responsible for bootstrapping configuration, storage, managers, commands
 * and listeners, and for cleaning up state on shutdown so no player is left
 * permanently invisible after a reload/restart.
 * <p>
 * PhantomVanish — made by Morteus
 */
public final class PhantomVanish extends JavaPlugin {

    /** Plugin author credit, also printed to console on enable. */
    private static final String AUTHOR = "Morteus";

    private static PhantomVanish instance;

    private ConfigManager configManager;
    private VanishStorage vanishStorage;
    private VanishManager vanishManager;
    private SpectatorManager spectatorManager;
    private ItemPickupManager itemPickupManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);

        this.vanishStorage = createStorage();
        this.vanishStorage.init();

        this.vanishManager = new VanishManager(this, vanishStorage);
        this.spectatorManager = new SpectatorManager(this);
        this.itemPickupManager = new ItemPickupManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("PhantomVanish v" + getDescription().getVersion() + " enabled. Made by " + AUTHOR);
    }

    @Override
    public void onDisable() {
        if (vanishManager != null) {
            vanishManager.restoreAllOnDisable();
        }
        if (vanishStorage != null) {
            vanishStorage.close();
        }
        getLogger().info("PhantomVanish disabled.");
    }

    /**
     * Builds the configured storage backend for persisting vanish state.
     * Defaults to the local YAML file if {@code storage.type} in
     * config.yml is missing or invalid.
     */
    private VanishStorage createStorage() {
        if (configManager.getStorageType() == StorageType.MYSQL) {
            return new MySQLVanishStorage(this);
        }
        return new YamlVanishStorage(this);
    }

    private void registerCommands() {
        VanishCommand vanishCommand = new VanishCommand(this);
        getCommand("vanish").setExecutor(vanishCommand);
        getCommand("vanish").setTabCompleter(vanishCommand);
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinListener(this), this);
        pluginManager.registerEvents(new PlayerQuitListener(this), this);
        pluginManager.registerEvents(new PlayerChangedWorldListener(this), this);
        pluginManager.registerEvents(new PlayerGameModeChangeListener(this), this);
        pluginManager.registerEvents(new SneakListener(this), this);
        pluginManager.registerEvents(new ItemPickupListener(this), this);
        pluginManager.registerEvents(new MobTargetListener(this), this);
        pluginManager.registerEvents(new VanishDamageListener(this), this);
    }

    public static PhantomVanish getInstance() {
        return instance;
    }

    public static String getAuthor() {
        return AUTHOR;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public VanishStorage getVanishStorage() {
        return vanishStorage;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public SpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public ItemPickupManager getItemPickupManager() {
        return itemPickupManager;
    }
}
