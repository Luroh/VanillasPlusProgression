package com.tuservidor.vanillaplus.fishing;

import com.tuservidor.vanillaplus.config.ConfigManager;
import com.tuservidor.vanillaplus.item.ItemFactory;
import com.tuservidor.vanillaplus.recipe.RecipeCategory;
import com.tuservidor.vanillaplus.unlock.UnlockManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class FishingManager implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final UnlockManager unlockManager;
    private final ItemFactory itemFactory;
    private final Random random = new Random();
    private final Map<UUID, Long> lastLegendary = new HashMap<>();

    public FishingManager(JavaPlugin plugin, ConfigManager config, UnlockManager unlockManager, ItemFactory itemFactory) {
        this.plugin = plugin;
        this.config = config;
        this.unlockManager = unlockManager;
        this.itemFactory = itemFactory;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (!unlockManager.isUnlocked(RecipeCategory.PESCA)) return;
        if (!(event.getCaught() instanceof Item caught)) return;
        Player player = event.getPlayer();
        ItemStack rod = player.getInventory().getItemInMainHand();
        int luck = rod.getEnchantmentLevel(Enchantment.LUCK_OF_THE_SEA);
        double roll = random.nextDouble() * 100.0;
        double legendary = Math.min(1.2, 0.6 * (1.0 + luck * 0.25));
        double epic = Math.min(4.8, 2.4 * (1.0 + luck * 0.20));
        double rare = Math.min(14.0, 7.0 * (1.0 + luck * 0.15));

        Biome biome = player.getLocation().getBlock().getBiome();
        boolean ocean = biome.name().contains("OCEAN");
        if (ocean) { rare *= 1.20; epic *= 1.20; legendary *= 1.10; }
        if (player.getWorld().hasStorm()) legendary *= 1.25;

        ItemStack result;
        if (roll < legendary && canLegendary(player)) {
            result = legendaryLoot(player);
            lastLegendary.put(player.getUniqueId(), System.currentTimeMillis());
        } else if (roll < legendary + epic) result = epicLoot();
        else if (roll < legendary + epic + rare) result = rareLoot();
        else if (roll < legendary + epic + rare + 20.0) result = uncommonLoot();
        else result = commonLoot();
        caught.setItemStack(result);
    }

    private boolean canLegendary(Player player) {
        long cd = config.main().getLong("fishing.legendary_cooldown_seconds", 45) * 1000L;
        return System.currentTimeMillis() - lastLegendary.getOrDefault(player.getUniqueId(), 0L) >= cd;
    }

    private ItemStack legendaryLoot(Player player) {
        if (player.getWorld().hasStorm() && random.nextBoolean()) return itemFactory.create("storm_pearl");
        if (random.nextDouble() < 0.70) return itemFactory.create("eel_heart");
        return new ItemStack(Material.HEART_OF_THE_SEA);
    }

    private ItemStack epicLoot() {
        int r = random.nextInt(4);
        if (r == 0) return itemFactory.create("living_coral");
        if (r == 1) return itemFactory.create("ancient_kelp");
        if (r == 2) return itemFactory.create("eel_heart");
        return new ItemStack(Material.CHEST);
    }

    private ItemStack rareLoot() {
        int r = random.nextInt(4);
        if (r == 0) return new ItemStack(Material.PRISMARINE_CRYSTALS);
        if (r == 1) return new ItemStack(Material.PRISMARINE_SHARD);
        if (r == 2) return itemFactory.create("ancient_kelp");
        return new ItemStack(Material.ENDER_PEARL);
    }

    private ItemStack uncommonLoot() {
        int r = random.nextInt(4);
        if (r == 0) return new ItemStack(Material.TROPICAL_FISH);
        if (r == 1) return itemFactory.create("living_coral_seed");
        if (r == 2) return itemFactory.create("amber_fungus_spore");
        return itemFactory.create("ancient_kelp_seed");
    }

    private ItemStack commonLoot() {
        int r = random.nextInt(4);
        if (r == 0) return new ItemStack(Material.COD);
        if (r == 1) return new ItemStack(Material.KELP);
        if (r == 2) return new ItemStack(Material.BONE_MEAL);
        return new ItemStack(Material.STRING);
    }
}
