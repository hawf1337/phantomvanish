package me.phantom.vanish.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.phantom.vanish.PhantomVanish;
import me.phantom.vanish.utils.ConfigManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * MySQL-backed storage for vanish state, intended for networks that run
 * PhantomVanish on multiple servers behind a proxy (BungeeCord/Velocity)
 * and want vanish state to stay consistent no matter which server a player
 * reconnects to.
 * <p>
 * Uses a small HikariCP connection pool. The table is created
 * automatically on first startup if it does not already exist.
 * <p>
 * If the connection cannot be established, the plugin logs an error and
 * simply runs without persistence for that session rather than crashing -
 * vanish still works, it just won't be remembered across a restart.
 */
public class MySQLVanishStorage implements VanishStorage {

    private final PhantomVanish plugin;
    private final String table;
    private HikariDataSource dataSource;

    public MySQLVanishStorage(PhantomVanish plugin) {
        this.plugin = plugin;
        this.table = sanitizeTableName(plugin.getConfigManager().getMysqlTable());
    }

    @Override
    public void init() {
        ConfigManager config = plugin.getConfigManager();

        String jdbcUrl = "jdbc:mysql://" + config.getMysqlHost() + ":" + config.getMysqlPort()
                + "/" + config.getMysqlDatabase() + config.getMysqlParameters();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getMysqlUsername());
        hikariConfig.setPassword(config.getMysqlPassword());
        hikariConfig.setPoolName("PhantomVanish-MySQL");
        hikariConfig.setMaximumPoolSize(4);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setIdleTimeout(600_000);
        hikariConfig.setMaxLifetime(1_800_000);

        try {
            dataSource = new HikariDataSource(hikariConfig);
            createTable();
            plugin.getLogger().info("Connected to MySQL storage (" + config.getMysqlHost() + ":"
                    + config.getMysqlPort() + "/" + config.getMysqlDatabase() + ").");
        } catch (Exception ex) {
            plugin.getLogger().severe("Could not connect to MySQL storage, vanish state will not be persisted this session: "
                    + ex.getMessage());
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
            dataSource = null;
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS `" + table + "` ("
                + "`uuid` VARCHAR(36) NOT NULL PRIMARY KEY"
                + ")";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    @Override
    public Set<UUID> loadVanishedPlayers() {
        Set<UUID> result = new HashSet<>();
        if (dataSource == null) {
            return result;
        }

        String sql = "SELECT `uuid` FROM `" + table + "`";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                try {
                    result.add(UUID.fromString(resultSet.getString("uuid")));
                } catch (IllegalArgumentException ignored) {
                    // Corrupt/invalid entry, skip it.
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().warning("Could not load vanished players from MySQL: " + ex.getMessage());
        }
        return result;
    }

    @Override
    public void addVanishedPlayer(UUID uuid) {
        if (dataSource == null) {
            return;
        }
        String sql = "INSERT IGNORE INTO `" + table + "` (`uuid`) VALUES (?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().warning("Could not save vanish state to MySQL: " + ex.getMessage());
        }
    }

    @Override
    public void removeVanishedPlayer(UUID uuid) {
        if (dataSource == null) {
            return;
        }
        String sql = "DELETE FROM `" + table + "` WHERE `uuid` = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().warning("Could not remove vanish state from MySQL: " + ex.getMessage());
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Keeps only safe characters in the configured table name, since it is
     * concatenated directly into SQL statements (it comes from the server
     * owner's own config.yml, not from untrusted player input, but this
     * guards against a copy-paste typo breaking the query).
     */
    private String sanitizeTableName(String rawName) {
        String cleaned = rawName == null ? "" : rawName.replaceAll("[^a-zA-Z0-9_]", "");
        return cleaned.isEmpty() ? "phantomvanish_vanished" : cleaned;
    }
}
