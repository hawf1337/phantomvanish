package me.phantom.vanish.storage;

import java.util.Set;
import java.util.UUID;

/**
 * Abstraction over the persistence backend used to remember which players
 * are vanished across restarts and disconnects.
 * <p>
 * Two implementations are provided: {@link YamlVanishStorage} (default,
 * local file, zero setup) and {@link MySQLVanishStorage} (shared database,
 * recommended for networks running PhantomVanish on multiple servers
 * behind BungeeCord/Velocity so vanish state stays consistent no matter
 * which server a player reconnects to).
 */
public interface VanishStorage {

    /**
     * Prepares the storage backend (opens the connection pool, creates the
     * table/file, etc). Called once on plugin startup, on the main thread.
     */
    void init();

    /**
     * Loads every currently vanished player's UUID.
     */
    Set<UUID> loadVanishedPlayers();

    /**
     * Persists that the given player is now vanished.
     */
    void addVanishedPlayer(UUID uuid);

    /**
     * Persists that the given player is no longer vanished.
     */
    void removeVanishedPlayer(UUID uuid);

    /**
     * Releases any resources held by the storage backend (connection pool,
     * open file handles, etc). Called once on plugin shutdown.
     */
    void close();
}
