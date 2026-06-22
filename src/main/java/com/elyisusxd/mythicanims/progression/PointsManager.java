package com.elyisusxd.mythicanims.progression;

import java.util.UUID;

/**
 * Fachada agnostica para la gestion de puntos y niveles de skill.
 * Delega todas las operaciones a un ProgressionDataSource.
 */
public class PointsManager {

    private final ProgressionDataSource dataSource;

    public PointsManager(ProgressionDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int getPoints(UUID uuid) {
        return dataSource.getPoints(uuid);
    }

    public void addPoints(UUID uuid, int amount) {
        if (amount != 0) {
            dataSource.addPoints(uuid, amount);
        }
    }

    public boolean spendPoints(UUID uuid, int amount) {
        return amount <= 0 || dataSource.spendPoints(uuid, amount);
    }

    public int getSkillLevel(UUID uuid, String skillId) {
        return dataSource.getSkillLevel(uuid, skillId);
    }

    public void setSkillLevel(UUID uuid, String skillId, int level) {
        dataSource.setSkillLevel(uuid, skillId, level);
    }

    public ProgressionDataSource getDataSource() {
        return dataSource;
    }
}
