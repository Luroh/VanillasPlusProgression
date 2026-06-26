package com.tuservidor.vanillaplus.command;

import com.tuservidor.vanillaplus.teleport.TpaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TpaCommands implements CommandExecutor, TabCompleter {
    private final TpaManager tpaManager;

    public TpaCommands(TpaManager tpaManager) {
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores.");
            return true;
        }
        switch (command.getName().toLowerCase()) {
            case "tpa" -> {
                if (args.length != 1) return false;
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    player.sendMessage("§cJugador no encontrado.");
                    return true;
                }
                tpaManager.request(player, target);
            }
            case "tpaccept" -> tpaManager.accept(player);
            case "tpdeny" -> tpaManager.deny(player);
            default -> { return false; }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("tpa") && args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());
            return names;
        }
        return List.of();
    }
}
