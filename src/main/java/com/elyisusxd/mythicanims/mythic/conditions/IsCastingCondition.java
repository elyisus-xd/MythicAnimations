package com.elyisusxd.mythicanims.mythic.conditions;

import com.elyisusxd.mythicanims.mythic.CastingTracker;
import com.elyisusxd.mythicanims.mythic.mechanics.CastLockMechanic;
import io.lumine.mythic.api.skills.SkillCaster;
import io.lumine.mythic.api.skills.conditions.ICasterCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.utils.annotations.MythicCondition;

@MythicCondition(author = "elyisusxd", name = "iscasting",
        description = "Retorna true/false segun si el caster tiene el aura CASTING activa")
public class IsCastingCondition extends SkillCondition implements ICasterCondition {

    public IsCastingCondition(String line) {
        super(line);
    }

    @Override
    public boolean check(SkillCaster caster) {
        if (caster == null || caster.getEntity() == null) return false;
        return CastingTracker.has(caster.getEntity().getUniqueId(), CastLockMechanic.CASTING_AURA);
    }
}
