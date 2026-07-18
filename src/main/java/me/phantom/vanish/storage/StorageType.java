package me.phantom.vanish.storage;

import java.util.logging.Logger;

/**
 * Which backend PhantomVanish uses to persist vanish state, configured via
 * {@code storage.type} in config.yml.
 */
public enum StorageType {
    YAML,
    MYSQL;

    /**
     * Safely parses a storage type from config, falling back (and warning)
     * to {@link #YAML} instead of throwing if the value is missing or
     * invalid.
     */
    public static StorageType fromConfig(String value, Logger logger) {
        if (value == null || value.isEmpty()) {
            return YAML;
        }
        try {
            return StorageType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            logger.warning("Invalid storage.type '" + value + "' in config.yml, defaulting to YAML.");
            return YAML;
        }
    }
}
