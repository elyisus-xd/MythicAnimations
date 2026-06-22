package com.elyisusxd.mythicanims.progression;

import com.elyisusxd.mythicanims.MythicAnimations;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Implementacion SQLite de ProgressionDataSource con cache en memoria.
 * Carga datos del jugador en PlayerJoinEvent, escribe async en cada cambio.
 */
public class LocalProgressionStorage implements ProgressionDataSource {

    private final MythicAnimations plugin;
    private final File dbFile;

    // Cache en memoria
    private final Map<UUID, Integer> pointsCache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> skillsCache = new ConcurrentHashMap<>();

    public LocalProgressionStorage(MythicAnimations plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "progression.db");
        plugin.getDataFolder().mkdirs();
        initDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }

    private void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_points (
                    uuid TEXT PRIMARY KEY,
                    points INTEGER NOT NULL DEFAULT 0
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS skill_levels (
                    uuid TEXT NOT NULL,
                    skill_id TEXT NOT NULL,
                    level INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (uuid, skill_id)
                )
            """);
            plugin.getLogger().info("Base de datos SQLite inicializada.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error inicializando SQLite", e);
        }
    }

    // --- Carga/descarga de jugador ---

    /**
     * Carga los datos del jugador desde SQLite a la cache. Llamar en PlayerJoinEvent.
     */
    public void loadPlayer(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String uuidStr = uuid.toString();
            try (Connection conn = getConnection()) {
                // Puntos
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT points FROM player_points WHERE uuid = ?")) {
                    ps.setString(1, uuidStr);
                    try (ResultSet rs = ps.executeQuery()) {
                        pointsCache.put(uuid, rs.next() ? rs.getInt("points") : 0);
                    }
                }
                // Skill levels
                Map<String, Integer> skills = new ConcurrentHashMap<>();
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT skill_id, level FROM skill_levels WHERE uuid = ?")) {
                    ps.setString(1, uuidStr);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            skills.put(rs.getString("skill_id"), rs.getInt("level"));
                        }
                    }
                }
                skillsCache.put(uuid, skills);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error cargando datos de " + uuid, e);
                pointsCache.putIfAbsent(uuid, 0);
                skillsCache.putIfAbsent(uuid, new ConcurrentHashMap<>());
            }
        });
    }

    /**
     * Descarga la cache del jugador. Llamar en PlayerQuitEvent.
     */
    public void unloadPlayer(UUID uuid) {
        pointsCache.remove(uuid);
        skillsCache.remove(uuid);
    }

    // --- ProgressionDataSource implementation ---

    @Override
    public int getSkillLevel(UUID uuid, String skillId) {
        Map<String, Integer> skills = skillsCache.get(uuid);
        if (skills == null) return 0;
        return skills.getOrDefault(skillId, 0);
    }

    @Override
    public void setSkillLevel(UUID uuid, String skillId, int level) {
        skillsCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(skillId, level);
        asyncSaveSkillLevel(uuid, skillId, level);
    }

    @Override
    public int getPoints(UUID uuid) {
        return pointsCache.getOrDefault(uuid, 0);
    }

    @Override
    public void addPoints(UUID uuid, int amount) {
        int newVal = pointsCache.merge(uuid, amount, Integer::sum);
        asyncSavePoints(uuid, newVal);
    }

    @Override
    public boolean spendPoints(UUID uuid, int amount) {
        int current = pointsCache.getOrDefault(uuid, 0);
        if (current < amount) return false;
        int newVal = current - amount;
        pointsCache.put(uuid, newVal);
        asyncSavePoints(uuid, newVal);
        return true;
    }

    // --- Escritura async ---

    private void asyncSavePoints(UUID uuid, int points) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT OR REPLACE INTO player_points (uuid, points) VALUES (?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, points);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error guardando puntos de " + uuid, e);
            }
        });
    }

    private void asyncSaveSkillLevel(UUID uuid, String skillId, int level) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT OR REPLACE INTO skill_levels (uuid, skill_id, level) VALUES (?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, skillId);
                ps.setInt(3, level);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Error guardando skill level de " + uuid, e);
            }
        });
    }
}
