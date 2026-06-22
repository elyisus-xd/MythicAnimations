package com.elyisusxd.mythicanims.mythic.conditions;

import com.elyisusxd.mythicanims.MythicAnimations;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.utils.annotations.MythicCondition;

@MythicCondition(author = "elyisusxd", name = "skilllevel",
        description = "Retorna true si el caster tiene el nivel exacto de la skill indicada")
public class SkillLevelCondition extends SkillCondition implements ICasterCondition {

    private final String skillId;
    private final int requiredLevel;

    public SkillLevelCondition(MythicConditionLoadEvent event) {
        super(event.getConditionName());
        MythicLineConfig mlc = event.getConfig();
        this.skillId = mlc.getString(new String[]{"skill", "s"}, "");
        this.requiredLevel = mlc.getInteger(new String[]{"level", "l"}, 0);
    }

    @Override
    public boolean check(SkillCaster caster) {
        if (caster == null || caster.getEntity() == null) return false;
        if (skillId.isEmpty()) return false;

        MythicAnimations plugin = MythicAnimations.inst();
        if (plugin == null || plugin.getPointsManager() == null) return false;

        int currentLevel = plugin.getPointsManager().getSkillLevel(
                caster.getEntity().getUniqueId(), skillId);
        return currentLevel == requiredLevel;
    }
}
