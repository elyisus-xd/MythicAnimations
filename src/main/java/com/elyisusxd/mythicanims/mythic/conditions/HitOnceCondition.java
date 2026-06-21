package com.elyisusxd.mythicanims.mythic.conditions;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import io.lumine.mythic.core.utils.annotations.MythicCondition;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@MythicCondition(author = "elyisusxd", name = "hitonce",
        description = "Retorna true solo la primera vez; aplica inmunidad interna por los ticks indicados")
public class HitOnceCondition extends SkillCondition implements IEntityCondition {

    private static final String AURA_NAME = "immunedelay";
    private static final Map<UUID, Long> IMMUNITY_EXPIRY = new ConcurrentHashMap<>();

    private final int immunityTicks;

    public HitOnceCondition(String line) {
        super(line);
        this.immunityTicks = parseImmunity(line);
    }

    @Override
    public boolean check(AbstractEntity e) {
        if (e == null) return false;
        UUID uuid = e.getUniqueId();
        long now = System.currentTimeMillis();

        Long expiry = IMMUNITY_EXPIRY.get(uuid);
        if (expiry != null && now < expiry) {
            // La aura "immunedelay" aun esta activa
            return false;
        }

        // Aplica la inmunidad como efecto secundario
        long expiryTime = now + (long) immunityTicks * 50L; // 1 tick = 50ms
        IMMUNITY_EXPIRY.put(uuid, expiryTime);
        return true;
    }

    /**
     * Parsea el atributo immunity/i de la linea de configuracion.
     * Formato esperado: hitonce{immunity=10} o hitonce{i=10}
     */
    private static int parseImmunity(String line) {
        if (line == null) return 10;
        // Busca immunity=N o i=N dentro de las llaves
        String search = line.toLowerCase();
        int start = search.indexOf('{');
        int end = search.indexOf('}');
        if (start < 0 || end < 0 || end <= start) return 10;

        String attrs = search.substring(start + 1, end);
        for (String attr : attrs.split(";")) {
            String[] kv = attr.split("=", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim();
            if ("immunity".equals(key) || "i".equals(key)) {
                try {
                    return Integer.parseInt(kv[1].trim());
                } catch (NumberFormatException ignored) {
                    return 10;
                }
            }
        }
        return 10;
    }

    /**
     * Limpia entradas expiradas. Puede llamarse periodicamente para evitar memory leaks.
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        IMMUNITY_EXPIRY.entrySet().removeIf(entry -> now >= entry.getValue());
    }
}
