package com.elyisusxd.mythicanims.gui;

import com.elyisusxd.mythicanims.MythicAnimations;
import com.elyisusxd.mythicanims.progression.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTreeGUI {

    public static final String TITLE = "§6Árbol de Habilidades";

    /**
     * Definicion de una skill dentro del arbol.
     */
    public record SkillDefinition(
            String id,
            String displayName,
            Material icon,
            int slot,
            int maxLevel,
            int costPerLevel
    ) {}

    /**
     * Construye el inventario del arbol de habilidades para el jugador.
     */
    public static Inventory build(Player player, MythicAnimations plugin) {
        Inventory inv = Bukkit.createInventory(null, 54,
                LegacyComponentSerializer.legacySection().deserialize(TITLE));

        // Relleno: GRAY_STAINED_GLASS_PANE sin nombre
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 54; i++) {
            inv.setItem(i, filler);
        }

        // Carga SkillDefinitions desde config
        Map<Integer, SkillDefinition> skillMap = loadSkills(plugin);
        PointsManager pm = plugin.getPointsManager();
        int playerPoints = pm != null ? pm.getPoints(player.getUniqueId()) : 0;

        // Coloca cada skill en su slot
        for (SkillDefinition def : skillMap.values()) {
            int currentLevel = pm != null ? pm.getSkillLevel(player.getUniqueId(), def.id()) : 0;
            boolean maxed = currentLevel >= def.maxLevel();

            ItemStack item = new ItemStack(def.icon());
            ItemMeta meta = item.getItemMeta();

            // Display name
            if (maxed) {
                meta.displayName(LegacyComponentSerializer.legacySection()
                        .deserialize("§6" + def.displayName()));
            } else {
                meta.displayName(LegacyComponentSerializer.legacySection()
                        .deserialize(def.displayName()));
            }

            // Lore
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Nivel: " + currentLevel + "/" + def.maxLevel(), NamedTextColor.GRAY));

            if (maxed) {
                lore.add(Component.text("¡Nivel máximo!", NamedTextColor.GOLD));
            } else {
                lore.add(Component.text("Costo siguiente: " + def.costPerLevel() + " puntos", NamedTextColor.YELLOW));
            }

            lore.add(Component.empty());
            lore.add(Component.text("Puntos disponibles: " + playerPoints, NamedTextColor.AQUA));
            meta.lore(lore);
            item.setItemMeta(meta);

            inv.setItem(def.slot(), item);
        }

        // Slot 49: info de puntos disponibles
        ItemStack pointsItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta pointsMeta = pointsItem.getItemMeta();
        pointsMeta.displayName(LegacyComponentSerializer.legacySection()
                .deserialize("§bPuntos: " + playerPoints));
        pointsItem.setItemMeta(pointsMeta);
        inv.setItem(49, pointsItem);

        return inv;
    }

    /**
     * Carga las SkillDefinitions desde config.yml (seccion skill-tree).
     */
    public static Map<Integer, SkillDefinition> loadSkills(MythicAnimations plugin) {
        Map<Integer, SkillDefinition> map = new HashMap<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("skill-tree");
        if (section == null) return map;

        for (String key : section.getKeys(false)) {
            ConfigurationSection skillSec = section.getConfigurationSection(key);
            if (skillSec == null) continue;

            String id = key;
            String displayName = skillSec.getString("display-name", id);
            Material icon;
            try {
                icon = Material.valueOf(skillSec.getString("icon", "BOOK").toUpperCase());
            } catch (IllegalArgumentException e) {
                icon = Material.BOOK;
            }
            int slot = skillSec.getInt("slot", 0);
            int maxLevel = skillSec.getInt("max-level", 3);
            int costPerLevel = skillSec.getInt("cost-per-level", 5);

            map.put(slot, new SkillDefinition(id, displayName, icon, slot, maxLevel, costPerLevel));
        }
        return map;
    }
}
