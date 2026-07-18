package me.phantom.vanish.storage;

import me.phantom.vanish.PhantomVanish;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Default storage backend: keeps the vanished-player list in a local
 * {@code vanished-players.yml} file inside the plugin's data folder.
 * Simple and requires no external setup, but is per-server only (does not
 * sync vanish state across a multi-server network).
 */
public class YamlVanishStorage implements VanishStorage {

    private final PhantomVanish plugin;
    private final Set<UUID> cache = new HashSet<>();

    private File dataFile;
    private YamlConfiguration dataConfig;

    public YamlVanishStorage(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "vanished-players.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                plugin.getLogger().warning("Could not create vanished-players.yml: " + ex.getMessage());
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String uuidString : dataConfig.getStringList("vanished-players")) {
            try {
                cache.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException ignored) {
                // Corrupt/invalid entry, skip it.
            }
        }
    }

    @Override
    public Set<UUID> loadVanishedPlayers() {
        return new HashSet<>(cache);
    }

    @Override
    public synchronized void addVanishedPlayer(UUID uuid) {
        cache.add(uuid);
        save();
    }

    @Override
    public synchronized void removeVanishedPlayer(UUID uuid) {
        cache.remove(uuid);
        save();
    }

    private void save() {
        List<String> uuidStrings = new ArrayList<>();
        for (UUID uuid : cache) {
            uuidStrings.add(uuid.toString());
        }
        dataConfig.set("vanished-players", uuidStrings);
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not save vanished-players.yml: " + ex.getMessage());
        }
    }

    @Override
    public void close() {
        // Nothing to release for a flat file.
    }
}
