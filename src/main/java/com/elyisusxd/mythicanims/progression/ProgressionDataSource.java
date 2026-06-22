package com.elyisusxd.mythicanims.progression;

import java.util.UUID;

/**
 * Interfaz para acceso a datos de progresion del jugador.
 * Implementacion por defecto: LocalProgressionStorage (SQLite).
 */
public interface ProgressionDataSource {

    /**
     * Obtiene el nivel actual de una skill para el jugador.
     *
     * @return nivel (0 = no aprendida)
     */
    int getSkillLevel(UUID uuid, String skillId);

    /**
     * Establece el nivel de una skill para el jugador.
     */
    void setSkillLevel(UUID uuid, String skillId, int level);

    /**
     * Obtiene los puntos de morph disponibles del jugador.
     */
    int getPoints(UUID uuid);

    /**
     * Agrega puntos al jugador.
     */
    void addPoints(UUID uuid, int amount);

    /**
     * Intenta gastar puntos. Retorna false si no hay suficientes.
     */
    boolean spendPoints(UUID uuid, int amount);
}
