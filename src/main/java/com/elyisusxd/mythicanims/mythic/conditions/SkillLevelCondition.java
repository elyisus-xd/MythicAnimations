package com.elyisusxd.mythicanims.mythic.conditions;

import com.elyisusxd.mythicanims.MythicAnimations;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.utils.annotations.MythicCondition;

@MythicCondition(author = "elyisusxd", name = "skilllevel",
        description = "Retorna true si el caster tiene el nivel exacto de la skill indicada")
public class SkillLevelCondition extends SkillCondition implements ICasterCondition {

    private final String skillId;
    private final int requiredLevel;

    public SkillLevelCondition(String line) {
        super(line);
        this.skillId = parseAttribute(line, "skill", "s", "");
        this.requiredLevel = parseIntAttribute(line, "level", "l", 0);
    }

    @Override
    public boolean check(SkillCaster caster) {
        if (caster == null || caster.getEntity() == null) return false;
        if (skillId.isEmpty()) return false;

        MythicAnimations plugin = MythicAnimations.inst();
        if (plugin == null || plugin.getPointsManager() == null) return false;

        int currentLevel = plugin.getPointsManager().getSkillLevel(
                caster.getEntity().getUniqueId(), skillId);
        return currentLevel >= requiredLevel;
    }

    // --- Parsing helpers para condiciones (sin MythicLineConfig) ---

    private static String parseAttribute(String line, String... keys) {
        if (line == null) return "";
        String defaultVal = keys.length >= 3 ? keys[2] : "";
        String longKey = keys[0];
        String shortKey = keys.length >= 2 ? keys[1] : keys[0];

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

    private static int parseIntAttribute(String line, String longKey, String shortKey, int defaultVal) {
        String val = parseAttribute(line, longKey, shortKey, String.valueOf(defaultVal));
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
