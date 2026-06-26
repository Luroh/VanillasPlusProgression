package com.tuservidor.vanillaplus.recipe;

import com.tuservidor.vanillaplus.config.MessageManager;
import com.tuservidor.vanillaplus.item.ItemFactory;
import com.tuservidor.vanillaplus.unlock.UnlockManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingInventory;

public class CraftingGuardListener implements Listener {
    private final RecipeRegistry registry;
    private final UnlockManager unlockManager;
    private final MessageManager messages;
    private final ItemFactory itemFactory;

    public CraftingGuardListener(RecipeRegistry registry, UnlockManager unlockManager, MessageManager messages, ItemFactory itemFactory) {
        this.registry = registry;
        this.unlockManager = unlockManager;
        this.messages = messages;
        this.itemFactory = itemFactory;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (!isRecipeAllowed(recipe)) event.getInventory().setResult(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!isRecipeAllowed(event.getRecipe())) {
            event.setCancelled(true);
            notify(event.getWhoClicked(), "recipe_locked");
        }
    }

    @EventHandler
    public void onPrepareSmith(PrepareSmithingEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null) return;
        String category = itemFactory.getCategory(result);
        RecipeCategory recipeCategory = RecipeCategory.from(category);
        if (recipeCategory != null && !unlockManager.isUnlocked(recipeCategory)) event.getInventory().setResult(null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSmithTake(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof SmithingInventory inv)) return;
        if (event.getRawSlot() != 3) return;
        ItemStack result = inv.getResult();
        if (result == null) return;
        RecipeCategory category = RecipeCategory.from(itemFactory.getCategory(result));
        if (category != null && !unlockManager.isUnlocked(category)) {
            event.setCancelled(true);
            notify(event.getWhoClicked(), "recipe_locked");
        }
    }

    private boolean isRecipeAllowed(Recipe recipe) {
        if (!(recipe instanceof org.bukkit.Keyed keyed)) return true;
        NamespacedKey key = keyed.getKey();
        RecipeCategory category = registry.categoryOf(key);
        return category == null || unlockManager.isUnlocked(category);
    }

    private void notify(HumanEntity entity, String key) {
        if (entity instanceof Player player) messages.send(player, key);
    }
}
