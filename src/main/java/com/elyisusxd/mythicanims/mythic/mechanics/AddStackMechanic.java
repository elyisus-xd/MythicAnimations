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

@MythicMechanic(author = "elyisusxd", name = "addstack",
        description = "Agrega stacks a un recurso nombrado con limite opcional")
public class AddStackMechanic implements INoTargetSkill {

    private final PlaceholderString resource;
    private final int max;
    private final int amount;

    public AddStackMechanic(MythicMechanicLoadEvent event) {
        MythicLineConfig mlc = event.getConfig();
        this.resource = PlaceholderString.of(
                mlc.getString(new String[]{"resource", "r"}, ""));
        this.max = mlc.getInteger(new String[]{"max", "m"}, -1);
        this.amount = mlc.getInteger(new String[]{"amount", "a"}, 1);
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        String res = resource.get(data);
        if (res.isEmpty()) return SkillResult.INVALID_CONFIG;

        AbstractEntity caster = data.getCaster().getEntity();
        if (caster == null) return SkillResult.ERROR;

        StackTracker.add(caster.getUniqueId(), res, amount, max);
        return SkillResult.SUCCESS;
    }
}
