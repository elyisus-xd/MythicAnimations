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
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import org.bukkit.Bukkit;

import com.elyisusxd.mythicanims.MythicAnimations;

import java.util.Optional;

@MythicMechanic(author = "elyisusxd", name = "vfx",
        description = "Spawnea un mob VFX en la ubicacion del caster, aplica state y lo remueve tras duration ticks")
public class VfxMechanic implements INoTargetSkill {

    private final PlaceholderString mobName;
    private final PlaceholderString state;
    private final int duration;
    private final boolean bright;

    public VfxMechanic(MythicMechanicLoadEvent event) {
        MythicLineConfig mlc = event.getConfig();
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
            // TODO: verificar método correcto para state machine en MM 5.x
            // setStance() es la forma estándar en MythicMobs 5.x para cambiar
            // el estado de una state machine de ModelEngine, pero si no funciona
            // se debe usar la API de ModelEngine directamente.
            activeMob.setStance(resolvedState);
        }

        // TODO: aplicar brightness via API cuando bright=true
        // MythicMobs/ModelEngine no expone un método directo para setear brightness
        // desde ActiveMob. La configuración de brillo se hace en el YAML del mob VFX
        // (ej: Options.LightLevel). Si se necesita control desde el mechanic,
        // habría que usar la API de ModelEngine directamente.
        // Por ahora se asume que el YAML del mob VFX ya tiene la brightness configurada.

        // Programa la remocion despues de duration ticks
        Bukkit.getScheduler().runTaskLater(
                MythicAnimations.inst(),
                activeMob::remove,
                duration
        );

        return SkillResult.SUCCESS;
    }
}
