package com.elyisusxd.mythicanims.mythic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilitario compartido para trackear auras internas por UUID y duracion en ticks.
 * Usado por CastLockMechanic, IsCastingCondition y futuros mechanics/conditions de aura.
 */
public final class CastingTracker {

    private static final Map<UUID, Long> ACTIVE_AURAS = new ConcurrentHashMap<>();

    private CastingTracker() {}

    /**
     * Aplica un aura interna al entity por la duracion indicada.
     *
     * @param uuid  UUID del entity
     * @param aura  nombre del aura (ej. "CASTING")
     * @param ticks duracion en ticks
     */
    public static void apply(UUID uuid, String aura, int ticks) {
        long expiry = System.currentTimeMillis() + (long) ticks * 50L;
        ACTIVE_AURAS.put(compositeKey(uuid, aura), expiry);
    }

    /**
     * Verifica si el entity tiene el aura activa (no expirada).
     */
    public static boolean has(UUID uuid, String aura) {
        Long expiry = ACTIVE_AURAS.get(compositeKey(uuid, aura));
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            ACTIVE_AURAS.remove(compositeKey(uuid, aura));
            return false;
        }
        return true;
    }

    /**
     * Remueve el aura del entity.
     */
    public static void remove(UUID uuid, String aura) {
        ACTIVE_AURAS.remove(compositeKey(uuid, aura));
    }

    /**
     * Limpia entradas expiradas para evitar memory leaks.
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        ACTIVE_AURAS.entrySet().removeIf(entry -> now >= entry.getValue());
    }

    private static UUID compositeKey(UUID uuid, String aura) {
        return UUID.nameUUIDFromBytes((uuid.toString() + ":" + aura).getBytes());
    }
}
