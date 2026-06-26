package com.tuservidor.vanillaplus.command;

import com.tuservidor.vanillaplus.teleport.HomeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HomeCommands implements CommandExecutor, TabCompleter {
    private final HomeManager homeManager;

    public HomeCommands(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores.");
            return true;
        }
        if (!homeManager.utilsUnlocked(player)) return true;
        String cmd = command.getName().toLowerCase();
        String name = args.length >= 1 ? args[0] : "home";
        switch (cmd) {
            case "sethome" -> homeManager.setHome(player, name);
            case "home" -> homeManager.teleportHome(player, name);
            case "delhome" -> homeManager.delHome(player, name);
            case "homes" -> {
                Set<String> homes = homeManager.listHomes(player.getUniqueId());
                player.sendMessage("§aHomes (" + homes.size() + "/" + homeManager.getLimit(player.getUniqueId()) + "): §f" + (homes.isEmpty() ? "ninguno" : String.join(", ", homes)));
            }
            default -> { return false; }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length == 1 && !command.getName().equalsIgnoreCase("sethome")) return new ArrayList<>(homeManager.listHomes(player.getUniqueId()));
        return List.of();
    }
}
