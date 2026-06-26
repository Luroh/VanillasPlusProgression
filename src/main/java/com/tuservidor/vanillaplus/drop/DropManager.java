package com.tuservidor.vanillaplus.drop;

import com.tuservidor.vanillaplus.config.ConfigManager;
import com.tuservidor.vanillaplus.item.ItemFactory;
import com.tuservidor.vanillaplus.recipe.RecipeCategory;
import com.tuservidor.vanillaplus.unlock.UnlockManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class DropManager implements Listener {
    private final ConfigManager config;
    private final UnlockManager unlockManager;
    private final ItemFactory itemFactory;
    private final Random random = new Random();

    public DropManager(ConfigManager config, UnlockManager unlockManager, ItemFactory itemFactory) {
        this.config = config;
        this.unlockManager = unlockManager;
        this.itemFactory = itemFactory;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        switch (entity.getType()) {
            case WITCH -> dropIfAllowed(event, RecipeCategory.MID, "temporal_core", 0.08, 1, 1);
            case PHANTOM -> dropIfAllowed(event, RecipeCategory.MID, "stalker_feather", 0.18, 1, 1);
            case WARDEN -> dropIfAllowed(event, RecipeCategory.LATE, "darkness_fragment", 1.0, 1, 2);
            case PIGLIN_BRUTE -> dropIfAllowed(event, RecipeCategory.LATE, "crimson_heart", 0.20, 1, 1);
            case MAGMA_CUBE -> dropIfAllowed(event, RecipeCategory.LATE, "infernal_ice", 0.04, 1, 1);
            case DROWNED -> {
                boolean trident = entity.getEquipment() != null && entity.getEquipment().getItemInMainHand().getType() == Material.TRIDENT;
                if (trident) dropIfAllowed(event, RecipeCategory.LATE, "storm_pearl", entity.getWorld().hasStorm() ? 0.20 : 0.10, 1, 1);
            }
            default -> {}
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.AMETHYST_CLUSTER && canDrop(RecipeCategory.MID) && random.nextDouble() < 0.06) {
            block.getWorld().dropItemNaturally(block.getLocation(), itemFactory.create("crystal_fragment"));
        }
    }

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        if (!canDrop(RecipeCategory.LATE)) return;
        if (event.getLootTable() == null || event.getLootTable().getKey() == null) return;
        String key = event.getLootTable().getKey().getKey();
        if (key.contains("bastion") && random.nextDouble() < 0.12) {
            event.getLoot().add(itemFactory.create("infernal_ice"));
        }
    }

    private void dropIfAllowed(EntityDeathEvent event, RecipeCategory category, String id, double chance, int min, int max) {
        if (!canDrop(category)) return;
        if (random.nextDouble() > chance) return;
        int amount = min + random.nextInt(Math.max(1, max - min + 1));
        ItemStack item = itemFactory.create(id);
        item.setAmount(amount);
        event.getDrops().add(item);
    }

    private boolean canDrop(RecipeCategory category) {
        return unlockManager.isUnlocked(category) || config.main().getBoolean("performance.allow_locked_drops", false);
    }
}
