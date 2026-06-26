package com.tuservidor.vanillaplus.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ItemUtil {
    private ItemUtil() {}

    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
    }

    public static void consumeOne(ItemStack item) {
        if (isEmpty(item)) return;
        item.setAmount(item.getAmount() - 1);
    }

    public static boolean isSword(Material material) {
        return material != null && material.name().endsWith("_SWORD");
    }
}
