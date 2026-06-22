package com.elyisusxd.mythicanims.mythic;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracker de recursos tipo stack (HEAT, CHARGES, etc.) por UUID.
 * Compartido entre AddStackMechanic y ConsumeStackMechanic.
 */
public final class StackTracker {

    private static final Map<String, Integer> STACKS = new ConcurrentHashMap<>();

    private StackTracker() {}

    /**
     * Agrega stacks al recurso, respetando un max opcional.
     *
     * @param uuid     UUID del entity
     * @param resource nombre del recurso
     * @param amount   cantidad a agregar
     * @param max      limite superior (-1 = sin limite)
     * @return nuevo valor de stacks
     */
    public static int add(UUID uuid, String resource, int amount, int max) {
        String key = key(uuid, resource);
        int current = STACKS.getOrDefault(key, 0);
        int newVal = current + amount;
        if (max >= 0 && newVal > max) newVal = max;
        STACKS.put(key, newVal);
        return newVal;
    }

    /**
     * Consume stacks del recurso.
     *
     * @param uuid     UUID del entity
     * @param resource nombre del recurso
     * @param amount   cantidad a consumir
     * @return true si habia suficientes stacks y se consumieron
     */
    public static boolean consume(UUID uuid, String resource, int amount) {
        String key = key(uuid, resource);
        int current = STACKS.getOrDefault(key, 0);
        if (current < amount) return false;
        int newVal = current - amount;
        if (newVal <= 0) {
            STACKS.remove(key);
        } else {
            STACKS.put(key, newVal);
        }
        return true;
    }

    /**
     * Obtiene el valor actual de stacks.
     */
    public static int get(UUID uuid, String resource) {
        return STACKS.getOrDefault(key(uuid, resource), 0);
    }

    /**
     * Resetea los stacks de un recurso a 0.
     */
    public static void reset(UUID uuid, String resource) {
        STACKS.remove(key(uuid, resource));
    }

    /**
     * Limpia todas las entradas. Usar con precaucion.
     */
    public static void clearAll() {
        STACKS.clear();
    }

    private static String key(UUID uuid, String resource) {
        return uuid.toString() + ":" + resource.toUpperCase();
    }
}
