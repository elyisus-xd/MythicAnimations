package com.elyisusxd.mythicanims.mythic.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.utils.annotations.MythicCondition;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@MythicCondition(author = "elyisusxd", name = "hitonce",
        description = "Retorna true solo la primera vez; aplica inmunidad interna por los ticks indicados")
public class HitOnceCondition extends SkillCondition implements IEntityCondition {

    private static final Map<UUID, Long> IMMUNITY_EXPIRY = new ConcurrentHashMap<>();

    private final int immunityTicks;

    public HitOnceCondition(MythicConditionLoadEvent event) {
        super(event.getConditionName());
        MythicLineConfig mlc = event.getConfig();
        this.immunityTicks = mlc.getInteger(new String[]{"immunity", "i"}, 10);
    }

    @Override
    public boolean check(AbstractEntity e) {
        if (e == null) return false;
        UUID uuid = e.getUniqueId();
        long now = System.currentTimeMillis();

        Long expiry = IMMUNITY_EXPIRY.get(uuid);
        if (expiry != null && now < expiry) {
            return false;
        }

        // Aplica la inmunidad como efecto secundario
        long expiryTime = now + (long) immunityTicks * 50L; // 1 tick = 50ms
        IMMUNITY_EXPIRY.put(uuid, expiryTime);
        return true;
    }

    /**
     * Limpia entradas expiradas. Puede llamarse periodicamente para evitar memory leaks.
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        IMMUNITY_EXPIRY.entrySet().removeIf(entry -> now >= entry.getValue());
    }
}
