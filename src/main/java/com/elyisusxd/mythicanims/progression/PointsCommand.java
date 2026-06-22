package com.elyisusxd.mythicanims.progression;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Comando /morphpoints para administrar puntos de progresion.
 * Subcomandos: give, set, get, reset
 * Permission: mythicanims.admin
 */
public class PointsCommand implements CommandExecutor, TabCompleter {

    private final PointsManager pointsManager;

    public PointsCommand(PointsManager pointsManager) {
        this.pointsManager = pointsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mythicanims.admin")) {
            sender.sendMessage(Component.text("No tienes permiso para usar este comando.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(Component.text("Jugador no encontrado: " + args[1], NamedTextColor.RED));
            return true;
        }

        UUID uuid = target.getUniqueId();

        switch (sub) {
            case "give" -> {
                if (args.length < 3) {
                    sendUsage(sender, label);
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    pointsManager.addPoints(uuid, amount);
                    sender.sendMessage(Component.text("Se dieron " + amount + " puntos a " + target.getName(), NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Cantidad invalida.", NamedTextColor.RED));
                }
            }
            case "set" -> {
                if (args.length < 3) {
                    sendUsage(sender, label);
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    int current = pointsManager.getPoints(uuid);
                    pointsManager.addPoints(uuid, amount - current);
                    sender.sendMessage(Component.text("Puntos de " + target.getName() + " establecidos a " + amount, NamedTextColor.GREEN));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Cantidad invalida.", NamedTextColor.RED));
                }
            }
            case "get" -> {
                int pts = pointsManager.getPoints(uuid);
                sender.sendMessage(Component.text(target.getName() + " tiene " + pts + " puntos.", NamedTextColor.YELLOW));
            }
            case "reset" -> {
                int current = pointsManager.getPoints(uuid);
                pointsManager.addPoints(uuid, -current);
                sender.sendMessage(Component.text("Puntos de " + target.getName() + " reseteados a 0.", NamedTextColor.GREEN));
            }
            default -> sendUsage(sender, label);
        }
        return true;
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage(Component.text("Uso: /" + label + " <give|set|get|reset> <jugador> [cantidad]", NamedTextColor.GRAY));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("give", "set", "get", "reset");
        }
        if (args.length == 2) {
            return null; // Bukkit auto-completa jugadores
        }
        return new ArrayList<>();
    }
}
