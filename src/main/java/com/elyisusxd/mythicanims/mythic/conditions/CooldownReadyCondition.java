package com.elyisusxd.mythicanims.mythic.conditions;

import com.elyisusxd.mythicanims.MythicAnimations;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.utils.annotations.MythicCondition;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@MythicCondition(author = "elyisusxd", name = "cooldownready",
        description = "Retorna true si el cooldown de la skill ha expirado; actualiza timestamp como efecto secundario")
public class CooldownReadyCondition extends SkillCondition implements ICasterCondition {

    private static final Map<String, Long> LAST_CAST = new ConcurrentHashMap<>();
    private static final long EXPIRY_THRESHOLD_MS = 10 * 60 * 1000L; // 10 minutos

    private final String skillId;
    private final double base;
    private final double reduction;

    public CooldownReadyCondition(MythicConditionLoadEvent event) {
        super(event.getConditionName());
        MythicLineConfig mlc = event.getConfig();
        this.skillId = mlc.getString(new String[]{"skill", "s"}, "");
        this.base = mlc.getDouble(new String[]{"base", "b"}, 8.0);
        this.reduction = mlc.getDouble(new String[]{"reduction", "r"}, 0.0);
    }

    @Override
    public boolean check(SkillCaster caster) {
        if (caster == null || caster.getEntity() == null) return false;
        if (skillId.isEmpty()) return false;

        UUID uuid = caster.getEntity().getUniqueId();
        String key = uuid.toString() + ":" + skillId;

        // Obtiene el nivel actual de la skill
        MythicAnimations plugin = MythicAnimations.inst();
        if (plugin == null || plugin.getPointsManager() == null) return false;

        int level = plugin.getPointsManager().getSkillLevel(uuid, skillId);
        if (level <= 0) level = 1;

        // Calcula cooldown efectivo: max(1.0, base - reduction * (level - 1))
        double effectiveCd = Math.max(1.0, base - reduction * (level - 1));
        long cdMillis = (long) (effectiveCd * 1000);

        long now = System.currentTimeMillis();
        long lastCast = LAST_CAST.getOrDefault(key, 0L);

        if ((now - lastCast) >= cdMillis) {
            // Cooldown expirado — actualiza timestamp (efecto secundario)
            LAST_CAST.put(key, now);
            return true;
        }

        return false;
    }

    /**
     * Limpia entradas con timestamp mayor a 10 minutos (jugadores offline, etc.)
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        LAST_CAST.entrySet().removeIf(entry -> (now - entry.getValue()) > EXPIRY_THRESHOLD_MS);
    }
}
