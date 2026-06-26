package com.tuservidor.vanillaplus.command;

import com.tuservidor.vanillaplus.config.MessageManager;
import com.tuservidor.vanillaplus.recipe.RecipeCategory;
import com.tuservidor.vanillaplus.unlock.UnlockManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Map;

public class UnlockCommand implements CommandExecutor, TabCompleter {
    private final UnlockManager unlockManager;
    private final MessageManager messages;

    public UnlockCommand(UnlockManager unlockManager, MessageManager messages) {
        this.unlockManager = unlockManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vanilla_plus.op")) {
            sender.sendMessage("§cNo tienes permiso.");
            return true;
        }
        if (args.length != 1) return false;
        RecipeCategory category = RecipeCategory.from(args[0]);
        UnlockManager.UnlockResult result = unlockManager.unlock(category);
        if (result == UnlockManager.UnlockResult.SUCCESS) {
            messages.send(sender, "unlock_success", Map.of("category", category.id()));
        } else if (result == UnlockManager.UnlockResult.ALREADY) {
            messages.send(sender, "unlock_already");
        } else if (result == UnlockManager.UnlockResult.DEPENDENCY) {
            RecipeCategory dependency = unlockManager.dependencyOf(category);
            messages.send(sender, "unlock_dependency", Map.of("dependency", dependency == null ? "unknown" : dependency.id()));
        } else {
            sender.sendMessage("§cUso: /unlockpp <early|mid|late|plantas|pesca|utils>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("early", "mid", "late", "plantas", "pesca", "utils");
        return List.of();
    }
}
