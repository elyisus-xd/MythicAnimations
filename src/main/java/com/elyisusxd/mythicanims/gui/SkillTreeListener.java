package com.elyisusxd.mythicanims.gui;

import com.elyisusxd.mythicanims.MythicAnimations;
import com.elyisusxd.mythicanims.progression.PointsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;

import java.util.Map;

public class SkillTreeListener implements Listener {

    private final MythicAnimations plugin;

    public SkillTreeListener(MythicAnimations plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Filtra por titulo del inventario
        InventoryView view = event.getView();
        Component title = view.title();
        String plainTitle = LegacyComponentSerializer.legacySection().serialize(title);
        if (!plainTitle.equals(SkillTreeGUI.TITLE)) return;

        // Cancela siempre (no mover items)
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Carga las skills para mapear slot -> definicion
        Map<Integer, SkillTreeGUI.SkillDefinition> skillMap = SkillTreeGUI.loadSkills(plugin);
        int slot = event.getRawSlot();

        SkillTreeGUI.SkillDefinition def = skillMap.get(slot);
        if (def == null) return; // Slot sin skill asignada

        PointsManager pm = plugin.getPointsManager();
        if (pm == null) return;

        int currentLevel = pm.getSkillLevel(player.getUniqueId(), def.id());
        if (currentLevel >= def.maxLevel()) {
            player.sendMessage(Component.text("Esta skill ya esta en nivel maximo.", NamedTextColor.RED));
            return;
        }

        // Intenta gastar los puntos (atomico)
        if (pm.spendPoints(player.getUniqueId(), def.costPerLevel())) {
            pm.setSkillLevel(player.getUniqueId(), def.id(), currentLevel + 1);
            player.sendMessage(Component.text("¡" + def.displayName() + " mejorada a nivel "
                    + (currentLevel + 1) + "!", NamedTextColor.GREEN));
            // Refresca el inventario
            player.openInventory(SkillTreeGUI.build(player, plugin));
        } else {
            player.sendMessage(Component.text("No tienes suficientes puntos. Necesitas "
                    + def.costPerLevel() + " puntos.", NamedTextColor.RED));
        }
    }
}
