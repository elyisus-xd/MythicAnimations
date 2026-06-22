package com.elyisusxd.mythicanims.mythic.conditions;

import com.elyisusxd.mythicanims.MythicAnimations;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
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

    public CooldownReadyCondition(String line) {
        super(line);
        this.skillId = parseAttribute(line, "skill", "s", "");
        this.base = parseDoubleAttribute(line, "base", "b", 8.0);
        this.reduction = parseDoubleAttribute(line, "reduction", "r", 0.0);
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

    // --- Parsing helpers para condiciones (sin MythicLineConfig) ---

    private static String parseAttribute(String line, String longKey, String shortKey, String defaultVal) {
        if (line == null) return defaultVal;
        String search = line.toLowerCase();
        int start = search.indexOf('{');
        int end = search.indexOf('}');
        if (start < 0 || end < 0 || end <= start) return defaultVal;

        String attrs = line.substring(start + 1, end);
        for (String attr : attrs.split(";")) {
            String[] kv = attr.split("=", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim().toLowerCase();
            if (key.equals(longKey) || key.equals(shortKey)) {
                return kv[1].trim();
            }
        }
        return defaultVal;
    }

    private static double parseDoubleAttribute(String line, String longKey, String shortKey, double defaultVal) {
        String val = parseAttribute(line, longKey, shortKey, String.valueOf(defaultVal));
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
