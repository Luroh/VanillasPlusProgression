package com.tuservidor.vanillaplus.item;

import org.bukkit.NamespacedKey;

public final class ItemKeys {
    private ItemKeys() {}

    public static final NamespacedKey ITEM_ID = new NamespacedKey("pp", "item_id");
    public static final NamespacedKey ITEM_TYPE = new NamespacedKey("pp", "item_type");
    public static final NamespacedKey SET_ID = new NamespacedKey("pp", "set_id");
    public static final NamespacedKey TIER = new NamespacedKey("pp", "tier");
    public static final NamespacedKey CATEGORY = new NamespacedKey("pp", "category");
    public static final NamespacedKey CUSTOM_VERSION = new NamespacedKey("pp", "custom_version");
    public static final NamespacedKey PROJECTILE_DAMAGE_MULTIPLIER = new NamespacedKey("pp", "projectile_damage_multiplier");
    public static final NamespacedKey PROJECTILE_RANGED_SET = new NamespacedKey("pp", "projectile_ranged_set");

    public static NamespacedKey recipe(String id) {
        String clean = id == null ? "unknown" : id;
        if (clean.startsWith("pp:")) clean = clean.substring(3);
        return new NamespacedKey("pp", clean.toLowerCase());
    }
}
