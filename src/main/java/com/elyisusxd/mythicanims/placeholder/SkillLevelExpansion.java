package com.elyisusxd.mythicanims.placeholder;

import com.elyisusxd.mythicanims.MythicAnimations;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Expansion de PlaceholderAPI que expone niveles de skill del jugador.
 * Formato: %mythicanims_skilllevel_<skillid>%
 *
 * Solo se registra si PlaceholderAPI esta presente (softdepend).
 */
public class SkillLevelExpansion extends PlaceholderExpansion {

    private final MythicAnimations plugin;

    public SkillLevelExpansion(MythicAnimations plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "elyisusxd";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mythicanims";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Requerido para expansiones internas
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        // %mythicanims_skilllevel_<skillid>%
        if (params.toLowerCase().startsWith("skilllevel_")) {
            String skillId = params.substring("skilllevel_".length());
            if (skillId.isEmpty()) return "0";

            if (plugin.getPointsManager() == null) return "0";

            int level = plugin.getPointsManager().getSkillLevel(player.getUniqueId(), skillId);
            return String.valueOf(level);
        }

        return null; // Placeholder desconocido
    }
}
