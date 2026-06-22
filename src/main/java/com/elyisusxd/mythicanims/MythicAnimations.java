package com.elyisusxd.mythicanims;

import com.elyisusxd.mythicanims.mythic.CastingTracker;
import com.elyisusxd.mythicanims.mythic.conditions.CooldownReadyCondition;
import com.elyisusxd.mythicanims.mythic.conditions.HitOnceCondition;
import com.elyisusxd.mythicanims.placeholder.SkillLevelExpansion;
import com.elyisusxd.mythicanims.progression.LocalProgressionStorage;
import com.elyisusxd.mythicanims.progression.PointsCommand;
import com.elyisusxd.mythicanims.progression.PointsManager;
import com.elyisusxd.mythicanims.progression.VanillaLevelListener;
import io.lumine.mythic.core.skills.CustomComponentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MythicAnimations extends JavaPlugin {

    private static MythicAnimations instance;
    private LocalProgressionStorage storage;
    private PointsManager pointsManager;

    @Override
    public void onEnable() {
        instance = this;

        Plugin mm = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (mm == null || !mm.isEnabled()) {
            getLogger().severe("MythicMobs no esta presente o no esta habilitado. Deshabilitando MythicAnimations.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Config por defecto
        saveDefaultConfig();

        // Registro de mechanics/conditions custom
        new CustomComponentRegistry(this, "com.elyisusxd.mythicanims.mythic");

        // Task periodico de cleanup async para evitar memory leaks (cada 5 minutos)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            CastingTracker.cleanup();
            HitOnceCondition.cleanup();
            CooldownReadyCondition.cleanup();
        }, 6000L, 6000L);

        // Sistema de progresion
        storage = new LocalProgressionStorage(this);
        pointsManager = new PointsManager(storage);

        // Listeners de ciclo de vida del jugador
        Bukkit.getPluginManager().registerEvents(
                new VanillaLevelListener(this, storage, pointsManager), this);

        // Comando /morphpoints
        PluginCommand morphpointsCmd = getCommand("morphpoints");
        if (morphpointsCmd != null) {
            PointsCommand cmd = new PointsCommand(pointsManager);
            morphpointsCmd.setExecutor(cmd);
            morphpointsCmd.setTabCompleter(cmd);
        }

        // PlaceholderAPI expansion (softdepend)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SkillLevelExpansion(this).register();
            getLogger().info("PlaceholderAPI detectado — expansion %mythicanims_skilllevel_*% registrada.");
        }

        getLogger().info("MythicAnimations habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("MythicAnimations deshabilitado.");
    }

    public static MythicAnimations inst() {
        return instance;
    }

    public PointsManager getPointsManager() {
        return pointsManager;
    }

    public LocalProgressionStorage getStorage() {
        return storage;
    }
}
