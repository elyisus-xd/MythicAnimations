package com.elyisusxd.mythicanims.mythic.mechanics;

import com.elyisusxd.mythicanims.mythic.StackTracker;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;

@MythicMechanic(author = "elyisusxd", name = "consumestack",
        description = "Consume stacks de un recurso nombrado; falla si no hay suficientes")
public class ConsumeStackMechanic implements INoTargetSkill {

    private final PlaceholderString resource;
    private final int amount;

    public ConsumeStackMechanic(MythicMechanicLoadEvent event) {
        MythicLineConfig mlc = event.getConfig();
        this.resource = PlaceholderString.of(
                mlc.getString(new String[]{"resource", "r"}, ""));
        this.amount = mlc.getInteger(new String[]{"amount", "a"}, 1);
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        String res = resource.get(data);
        if (res.isEmpty()) return SkillResult.INVALID_CONFIG;

        AbstractEntity caster = data.getCaster().getEntity();
        if (caster == null) return SkillResult.ERROR;

        boolean ok = StackTracker.consume(caster.getUniqueId(), res, amount);
        return ok ? SkillResult.SUCCESS : SkillResult.ERROR;
    }
}
