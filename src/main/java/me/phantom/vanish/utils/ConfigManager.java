package me.phantom.vanish.utils;

import me.phantom.vanish.PhantomVanish;
import me.phantom.vanish.storage.StorageType;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads and exposes all values defined in {@code config.yml}.
 * <p>
 * Centralizing configuration access here keeps the rest of the codebase
 * free of raw {@code getConfig()} calls and makes future config changes
 * easy to maintain.
 */
public class ConfigManager {

    private final PhantomVanish plugin;

    private long doubleShiftTime;
    private boolean spectatorEnabled;
    private boolean autoFlyOnSpectatorExit;
    private long vanishActionBarInterval;
    private boolean silentJoinQuit;
    private boolean blockDamage;
    private boolean blockMobTargeting;

    private StorageType storageType;
    private String mysqlHost;
    private int mysqlPort;
    private String mysqlDatabase;
    private String mysqlUsername;
    private String mysqlPassword;
    private String mysqlTable;
    private String mysqlParameters;

    private String permissionVanish;
    private String permissionVanishSee;
    private String permissionVanishItem;

    private String messageVanishEnabled;
    private String messageVanishDisabled;
    private String messageVanishReminder;
    private String messageSpectatorEnabled;
    private String messageSpectatorDisabled;
    private String messageNoPermission;
    private String messagePlayerOnly;
    private String messageItemPickupBlockEnabled;
    private String messageItemPickupBlockDisabled;
    private String messageVanishListHeader;
    private String messageVanishListEmpty;
    private String messageVanishListEntryPrefix;
    private List<String> helpMessages;

    private Sound soundVanishEnable;
    private Sound soundVanishDisable;
    private Sound soundSpectatorEnter;
    private Sound soundSpectatorExit;
    private Sound soundItemPickupToggle;
    private float soundVolume;
    private float soundPitch;

    public ConfigManager(PhantomVanish plugin) {
        this.plugin = plugin;
        load();
    }

    /**
     * (Re)loads every configuration value from disk. Safe to call again
     * later if a "/phantomvanish reload" style command is ever added.
     */
    public void load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.doubleShiftTime = config.getLong("double-shift-time", 400L);
        this.vanishActionBarInterval = config.getLong("vanish-actionbar-interval", 40L);

        this.spectatorEnabled = config.getBoolean("spectator.enabled", true);
        this.autoFlyOnSpectatorExit = config.getBoolean("spectator.auto-fly-on-exit", true);

        this.silentJoinQuit = config.getBoolean("vanish.silent-join-quit", true);
        this.blockDamage = config.getBoolean("vanish.block-damage", true);
        this.blockMobTargeting = config.getBoolean("vanish.block-mob-targeting", true);

        this.storageType = StorageType.fromConfig(config.getString("storage.type", "YAML"), plugin.getLogger());
        this.mysqlHost = config.getString("storage.mysql.host", "localhost");
        this.mysqlPort = config.getInt("storage.mysql.port", 3306);
        this.mysqlDatabase = config.getString("storage.mysql.database", "phantomvanish");
        this.mysqlUsername = config.getString("storage.mysql.username", "root");
        this.mysqlPassword = config.getString("storage.mysql.password", "");
        this.mysqlTable = config.getString("storage.mysql.table", "phantomvanish_vanished");
        this.mysqlParameters = config.getString("storage.mysql.parameters",
                "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=utf-8");

        this.permissionVanish = config.getString("permissions.vanish", "phantomvanish.vanish");
        this.permissionVanishSee = config.getString("permissions.vanish-see", "phantomvanish.vanish.see");
        this.permissionVanishItem = config.getString("permissions.vanish-item", "phantomvanish.vanish.item");

        this.messageVanishEnabled = config.getString("messages.vanish-enabled", "&a✔ Vanish enabled");
        this.messageVanishDisabled = config.getString("messages.vanish-disabled", "&c✖ Vanish disabled");
        this.messageVanishReminder = config.getString("messages.vanish-reminder",
                "&e⚠ Reminder: you are currently in vanish mode! Use /v to turn it off.");
        this.messageSpectatorEnabled = config.getString("messages.spectator-enabled", "&e👁 Spectator mode enabled");
        this.messageSpectatorDisabled = config.getString("messages.spectator-disabled", "&e👁 Restored to previous game mode");
        this.messageNoPermission = config.getString("messages.no-permission", "&cYou don't have permission to use this command.");
        this.messagePlayerOnly = config.getString("messages.player-only", "&cThis command can only be used by a player.");
        this.messageItemPickupBlockEnabled = config.getString("messages.item-pickup-block-enabled",
                "&a✔ Item pickup blocking enabled &7(you can't pick up items while vanished)");
        this.messageItemPickupBlockDisabled = config.getString("messages.item-pickup-block-disabled",
                "&c✖ Item pickup blocking disabled &7(you can pick up items while vanished)");
        this.messageVanishListHeader = config.getString("messages.vanish-list-header", "&b&lPlayers currently in vanish:");
        this.messageVanishListEmpty = config.getString("messages.vanish-list-empty", "&7Nobody is currently vanished.");
        this.messageVanishListEntryPrefix = config.getString("messages.vanish-list-entry-prefix", "&8 » &f");

        List<String> defaultHelp = List.of(
                "&8&m----------------------------------------",
                "&b&lPhantomVanish &7- Help",
                "&e/v &8» &7Toggle vanish on or off",
                "&e/v help &8» &7This help menu",
                "&e/v item &8» &7Toggle item pickup blocking while vanished",
                "&e/v list &8» &7List players currently in vanish",
                "&e2x Shift &8» &7While vanished: toggle spectator mode",
                "&8Made by: Morteus",
                "&8&m----------------------------------------"
        );
        this.helpMessages = new ArrayList<>(config.getStringList("messages.help"));
        if (this.helpMessages.isEmpty()) {
            this.helpMessages = new ArrayList<>(defaultHelp);
        }

        this.soundVanishEnable = parseSound(config.getString("sounds.vanish-enable"), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        this.soundVanishDisable = parseSound(config.getString("sounds.vanish-disable"), Sound.ENTITY_ITEM_BREAK);
        this.soundSpectatorEnter = parseSound(config.getString("sounds.spectator-enter"), Sound.BLOCK_BEACON_ACTIVATE);
        this.soundSpectatorExit = parseSound(config.getString("sounds.spectator-exit"), Sound.BLOCK_BEACON_DEACTIVATE);
        this.soundItemPickupToggle = parseSound(config.getString("sounds.item-pickup-toggle"), Sound.UI_BUTTON_CLICK);

        this.soundVolume = (float) config.getDouble("sounds.volume", 1.0);
        this.soundPitch = (float) config.getDouble("sounds.pitch", 1.0);
    }

    /**
     * Safely parses a Sound enum name from config, falling back (and warning)
     * if the value is missing or invalid instead of throwing at startup.
     */
    private Sound parseSound(String name, Sound fallback) {
        if (name == null || name.isEmpty()) {
            return fallback;
        }
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid sound '" + name + "' in config.yml, using default instead.");
            return fallback;
        }
    }

    public long getDoubleShiftTime() {
        return doubleShiftTime;
    }

    public boolean isSpectatorEnabled() {
        return spectatorEnabled;
    }

    public boolean isAutoFlyOnSpectatorExit() {
        return autoFlyOnSpectatorExit;
    }

    public long getVanishActionBarInterval() {
        return vanishActionBarInterval;
    }

    public boolean isSilentJoinQuit() {
        return silentJoinQuit;
    }

    public boolean isBlockDamage() {
        return blockDamage;
    }

    public boolean isBlockMobTargeting() {
        return blockMobTargeting;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUsername() {
        return mysqlUsername;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getMysqlTable() {
        return mysqlTable;
    }

    public String getMysqlParameters() {
        return mysqlParameters;
    }

    public String getPermissionVanish() {
        return permissionVanish;
    }

    public String getPermissionVanishSee() {
        return permissionVanishSee;
    }

    public String getPermissionVanishItem() {
        return permissionVanishItem;
    }

    public String getMessageVanishEnabled() {
        return messageVanishEnabled;
    }

    public String getMessageVanishDisabled() {
        return messageVanishDisabled;
    }

    public String getMessageVanishReminder() {
        return messageVanishReminder;
    }

    public String getMessageSpectatorEnabled() {
        return messageSpectatorEnabled;
    }

    public String getMessageSpectatorDisabled() {
        return messageSpectatorDisabled;
    }

    public String getMessageNoPermission() {
        return messageNoPermission;
    }

    public String getMessagePlayerOnly() {
        return messagePlayerOnly;
    }

    public String getMessageItemPickupBlockEnabled() {
        return messageItemPickupBlockEnabled;
    }

    public String getMessageItemPickupBlockDisabled() {
        return messageItemPickupBlockDisabled;
    }

    public String getMessageVanishListHeader() {
        return messageVanishListHeader;
    }

    public String getMessageVanishListEmpty() {
        return messageVanishListEmpty;
    }

    public String getMessageVanishListEntryPrefix() {
        return messageVanishListEntryPrefix;
    }

    public List<String> getHelpMessages() {
        return Collections.unmodifiableList(helpMessages);
    }

    public Sound getSoundVanishEnable() {
        return soundVanishEnable;
    }

    public Sound getSoundVanishDisable() {
        return soundVanishDisable;
    }

    public Sound getSoundSpectatorEnter() {
        return soundSpectatorEnter;
    }

    public Sound getSoundSpectatorExit() {
        return soundSpectatorExit;
    }

    public Sound getSoundItemPickupToggle() {
        return soundItemPickupToggle;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getSoundPitch() {
        return soundPitch;
    }
}
