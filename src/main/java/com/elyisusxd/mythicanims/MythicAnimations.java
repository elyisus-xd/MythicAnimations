package com.elyisusxd.mythicanims;

import io.lumine.mythic.core.skills.CustomComponentRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class MythicAnimations extends JavaPlugin {

    private static MythicAnimations instance;

    @Override
    public void onEnable() {
        instance = this;

        Plugin mm = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if (mm == null || !mm.isEnabled()) {
            getLogger().severe("MythicMobs no esta presente o no esta habilitado. Deshabilitando MythicAnimations.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new CustomComponentRegistry(this, "com.elyisusxd.mythicanims.mythic");

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
}
