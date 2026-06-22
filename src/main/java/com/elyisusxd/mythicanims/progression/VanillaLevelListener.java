package com.elyisusxd.mythicanims.progression;

import com.elyisusxd.mythicanims.MythicAnimations;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener de eventos de ciclo de vida del jugador para progresion.
 * - PlayerJoinEvent: carga datos desde SQLite
 * - PlayerQuitEvent: descarga cache
 * - PlayerLevelChangeEvent: otorga puntos al subir de nivel vanilla
 */
public class VanillaLevelListener implements Listener {

    private final MythicAnimations plugin;
    private final LocalProgressionStorage storage;
    private final PointsManager pointsManager;

    public VanillaLevelListener(MythicAnimations plugin, LocalProgressionStorage storage, PointsManager pointsManager) {
        this.plugin = plugin;
        this.storage = storage;
        this.pointsManager = pointsManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        storage.loadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        storage.unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLevelChange(PlayerLevelChangeEvent event) {
        int oldLevel = event.getOldLevel();
        int newLevel = event.getNewLevel();
        if (newLevel <= oldLevel) return;

        int pointsPerLevel = plugin.getConfig().getInt("points-per-level", 1);
        int levelsGained = newLevel - oldLevel;
        int pointsGained = levelsGained * pointsPerLevel;

        if (pointsGained > 0) {
            Player player = event.getPlayer();
            pointsManager.addPoints(player.getUniqueId(), pointsGained);
        }
    }
}
