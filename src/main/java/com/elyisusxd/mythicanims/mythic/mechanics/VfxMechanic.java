package com.elyisusxd.mythicanims.mythic.mechanics;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import org.bukkit.Bukkit;

import com.elyisusxd.mythicanims.MythicAnimations;

import java.io.File;
import java.util.Optional;

@MythicMechanic(author = "elyisusxd", name = "vfx",
        description = "Spawnea un mob VFX en la ubicacion del caster, aplica state y lo remueve tras duration ticks")
public class VfxMechanic extends SkillMechanic implements INoTargetSkill {

    private final PlaceholderString mobName;
    private final PlaceholderString state;
    private final int duration;
    private final boolean bright;

    public VfxMechanic(SkillExecutor manager, File file, String line, MythicLineConfig mlc) {
        super(manager, file, line, mlc);
        this.mobName = PlaceholderString.of(
                mlc.getString(new String[]{"mob", "m"}, ""));
        this.state = PlaceholderString.of(
                mlc.getString(new String[]{"state", "s"}, ""));
        this.duration = mlc.getInteger(new String[]{"duration", "d"}, 40);
        this.bright = mlc.getBoolean(new String[]{"bright", "b"}, true);
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        String mob = mobName.get(data);
        if (mob.isEmpty()) return SkillResult.INVALID_CONFIG;

        Optional<MythicMob> optMob = MythicBukkit.inst().getMobManager().getMythicMob(mob);
        if (optMob.isEmpty()) return SkillResult.INVALID_CONFIG;

        AbstractEntity caster = data.getCaster().getEntity();
        if (caster == null) return SkillResult.ERROR;
        AbstractLocation location = caster.getLocation();

        // Spawnea el VFX mob en la ubicacion
        ActiveMob activeMob = optMob.get().spawn(location, 1);
        if (activeMob == null) return SkillResult.ERROR;

        // Aplica state (Stance del state machine de ModelEngine)
        String resolvedState = state.get(data);
        if (!resolvedState.isEmpty()) {
            activeMob.setStance(resolvedState);
        }

        // Programa la remocion despues de duration ticks
        Bukkit.getScheduler().runTaskLater(
                MythicAnimations.inst(),
                activeMob::remove,
                duration
        );

        return SkillResult.SUCCESS;
    }
}
