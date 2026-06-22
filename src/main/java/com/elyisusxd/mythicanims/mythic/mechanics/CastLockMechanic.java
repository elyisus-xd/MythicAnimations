package com.elyisusxd.mythicanims.mythic.mechanics;

import com.elyisusxd.mythicanims.mythic.CastingTracker;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;

import java.io.File;

@MythicMechanic(author = "elyisusxd", name = "castlock",
        description = "Aplica el aura interna CASTING por la duracion indicada")
public class CastLockMechanic extends SkillMechanic implements INoTargetSkill {

    public static final String CASTING_AURA = "CASTING";

    private final int duration;

    public CastLockMechanic(SkillExecutor manager, File file, String line, MythicLineConfig mlc) {
        super(manager, file, line, mlc);
        this.duration = mlc.getInteger(new String[]{"duration", "d"}, 10);
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        AbstractEntity caster = data.getCaster().getEntity();
        if (caster == null) return SkillResult.ERROR;

        CastingTracker.apply(caster.getUniqueId(), CASTING_AURA, duration);
        return SkillResult.SUCCESS;
    }
}
