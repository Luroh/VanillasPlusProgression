package com.tuservidor.vanillaplus.unlock;

import com.tuservidor.vanillaplus.data.DataStore;
import com.tuservidor.vanillaplus.recipe.RecipeCategory;
import com.tuservidor.vanillaplus.recipe.RecipeRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class UnlockManager implements Listener {
    public enum UnlockResult { SUCCESS, ALREADY, DEPENDENCY, INVALID }

    private final JavaPlugin plugin;
    private final DataStore store;
    private RecipeRegistry recipeRegistry;
    private final Map<RecipeCategory, Boolean> unlocked = new EnumMap<>(RecipeCategory.class);

    public UnlockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.store = new DataStore(plugin, "data/unlocks.yml");
        load();
    }

    public void setRecipeRegistry(RecipeRegistry recipeRegistry) {
        this.recipeRegistry = recipeRegistry;
    }

    public void load() {
        for (RecipeCategory category : RecipeCategory.values()) {
            boolean value = store.get().getBoolean("unlocks." + category.id(), false);
            unlocked.put(category, value);
            if (!store.get().contains("unlocks." + category.id())) store.get().set("unlocks." + category.id(), false);
        }
        store.save();
    }

    public boolean isUnlocked(RecipeCategory category) {
        return unlocked.getOrDefault(category, false);
    }

    public UnlockResult unlock(RecipeCategory category) {
        if (category == null) return UnlockResult.INVALID;
        if (isUnlocked(category)) return UnlockResult.ALREADY;
        RecipeCategory dependency = dependencyOf(category);
        if (dependency != null && !isUnlocked(dependency)) return UnlockResult.DEPENDENCY;
        unlocked.put(category, true);
        store.get().set("unlocks." + category.id(), true);
        store.save();
        discoverCategory(category);
        return UnlockResult.SUCCESS;
    }

    public RecipeCategory dependencyOf(RecipeCategory category) {
        if (category == RecipeCategory.MID) return RecipeCategory.EARLY;
        if (category == RecipeCategory.LATE) return RecipeCategory.MID;
        return null;
    }

    public void discoverAllUnlocked(Player player) {
        if (recipeRegistry == null) return;
        for (RecipeCategory category : RecipeCategory.values()) {
            if (isUnlocked(category)) discover(player, recipeRegistry.keysFor(category));
        }
    }

    public void discoverCategory(RecipeCategory category) {
        if (recipeRegistry == null) return;
        List<NamespacedKey> keys = recipeRegistry.keysFor(category);
        for (Player player : Bukkit.getOnlinePlayers()) discover(player, keys);
    }

    private void discover(Player player, List<NamespacedKey> keys) {
        if (keys == null || keys.isEmpty()) return;
        try {
            player.discoverRecipes(keys);
        } catch (Exception ignored) {
            for (NamespacedKey key : keys) player.discoverRecipe(key);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> discoverAllUnlocked(event.getPlayer()), 20L);
    }
}
