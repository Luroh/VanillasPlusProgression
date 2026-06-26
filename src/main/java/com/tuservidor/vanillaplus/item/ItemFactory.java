package com.tuservidor.vanillaplus.item;

import com.tuservidor.vanillaplus.util.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class ItemFactory {
    private final Map<String, CustomItemSpec> specs = new LinkedHashMap<>();

    public ItemFactory() {
        registerAll();
    }

    public Collection<CustomItemSpec> specs() {
        return Collections.unmodifiableCollection(specs.values());
    }

    public CustomItemSpec spec(String rawId) {
        return specs.get(clean(rawId));
    }

    public boolean exists(String rawId) {
        return specs.containsKey(clean(rawId));
    }

    public ItemStack create(String rawId) {
        CustomItemSpec spec = spec(rawId);
        if (spec == null) throw new IllegalArgumentException("Item custom no existe: " + rawId);
        ItemStack item = new ItemStack(spec.material());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(colorByTier(spec.tier()) + spec.name());
            meta.setCustomModelData(spec.customModelData());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_GRAY + "ID: pp:" + spec.id());
            if (!"none".equals(spec.setId())) lore.add(ChatColor.GRAY + "Set: " + displaySet(spec.setId()));
            if (spec.tier() != null) lore.add(ChatColor.GRAY + "Tier: " + spec.tier());
            if ("early".equals(spec.tier())) lore.add(ChatColor.BLUE + "Equivalente base: Diamante");
            if ("mid".equals(spec.tier())) lore.add(ChatColor.DARK_PURPLE + "Equivalente base: Netherite");
            if ("late".equals(spec.tier())) lore.add(ChatColor.GOLD + "Superior a Netherite por balance custom");
            if ("amulet".equals(spec.type())) lore.add(ChatColor.YELLOW + "Amuleto pasivo: usar en offhand");
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(ItemKeys.ITEM_ID, PersistentDataType.STRING, spec.id());
            pdc.set(ItemKeys.ITEM_TYPE, PersistentDataType.STRING, spec.type());
            pdc.set(ItemKeys.CATEGORY, PersistentDataType.STRING, spec.category());
            if (!"none".equals(spec.setId())) pdc.set(ItemKeys.SET_ID, PersistentDataType.STRING, spec.setId());
            if (spec.tier() != null) pdc.set(ItemKeys.TIER, PersistentDataType.STRING, spec.tier());
            pdc.set(ItemKeys.CUSTOM_VERSION, PersistentDataType.INTEGER, 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isCustom(ItemStack item) {
        return getItemId(item) != null;
    }

    public boolean isCustom(ItemStack item, String rawId) {
        String id = getItemId(item);
        return id != null && id.equals(clean(rawId));
    }

    public String getItemId(ItemStack item) { return readString(item, ItemKeys.ITEM_ID); }
    public String getItemType(ItemStack item) { return readString(item, ItemKeys.ITEM_TYPE); }
    public String getSetId(ItemStack item) { return readString(item, ItemKeys.SET_ID); }
    public String getTier(ItemStack item) { return readString(item, ItemKeys.TIER); }
    public String getCategory(ItemStack item) { return readString(item, ItemKeys.CATEGORY); }

    private String readString(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private String clean(String id) {
        if (id == null) return null;
        return id.startsWith("pp:") ? id.substring(3) : id;
    }

    private ChatColor colorByTier(String tier) {
        if ("late".equals(tier)) return ChatColor.GOLD;
        if ("mid".equals(tier)) return ChatColor.AQUA;
        if ("early".equals(tier)) return ChatColor.GREEN;
        return ChatColor.WHITE;
    }

    private String displaySet(String set) {
        return Text.stripNamespace(set).replace('_', ' ');
    }

    private void add(String id, String name, String category, String set, int model, String type, Material material, String tier) {
        specs.put(id, new CustomItemSpec(id, name, category, set, model, type, material, tier));
    }

    private void registerSet(String set, String visible, String tier, int startModel, boolean crossbow) {
        String category = tier;
        Material helmet = armorMaterial(tier, "helmet");
        Material chest = armorMaterial(tier, "chestplate");
        Material legs = armorMaterial(tier, "leggings");
        Material boots = armorMaterial(tier, "boots");
        Material sword = swordMaterial(tier);
        add(set + "_helmet", "Casco de " + visible, category, set, startModel, "armor", helmet, tier);
        add(set + "_chestplate", "Pechera de " + visible, category, set, startModel + 1, "armor", chest, tier);
        add(set + "_leggings", "Pantalones de " + visible, category, set, startModel + 2, "armor", legs, tier);
        add(set + "_boots", "Botas de " + visible, category, set, startModel + 3, "armor", boots, tier);
        add(set + "_sword", "Espada de " + visible, category, set, startModel + 4, "sword", sword, tier);
        add(set + "_amulet", "Amuleto de " + visible, category, set, startModel + 5, "amulet", Material.AMETHYST_SHARD, tier);
        if (crossbow) add(set + "_crossbow", "Ballesta de " + visible, category, set, startModel + 6, "crossbow", Material.CROSSBOW, tier);
        else add(set + "_bow", "Arco de " + visible, category, set, startModel + 6, "bow", Material.BOW, tier);
    }

    private Material armorMaterial(String tier, String part) {
        String prefix = "early".equals(tier) ? "DIAMOND_" : "NETHERITE_";
        return Material.valueOf(prefix + part.toUpperCase(Locale.ROOT));
    }

    private Material swordMaterial(String tier) {
        return "early".equals(tier) ? Material.DIAMOND_SWORD : Material.NETHERITE_SWORD;
    }

    private void registerAll() {
        registerSet("pluma", "Pluma", "early", 1000, false);
        registerSet("musgo", "Musgo", "early", 1007, false);
        registerSet("nieve", "Nieve", "early", 1014, true);
        registerSet("miel", "Miel", "early", 1021, false);
        registerSet("tiempo_rojo", "Tiempo Rojo", "mid", 1028, false);
        registerSet("arquero", "Arquero", "mid", 1035, false);
        registerSet("metal_ligero", "Metal Ligero", "mid", 1042, false);
        registerSet("acero_cristalizado", "Acero Cristalizado", "mid", 1049, true);
        registerSet("diamante_oscuro", "Diamante Oscuro", "late", 1056, true);
        registerSet("oceano_electrico", "Oceano Electrico", "late", 1063, false);
        registerSet("calista_sangre", "Calista de Sangre", "late", 1070, false);
        registerSet("nieve_abajo", "Nieve de Abajo", "late", 1077, false);

        add("home_crystal", "Home Crystal", "utils", "none", 2000, "utility", Material.DIAMOND, null);
        add("mid_upgrade_template", "Plantilla de Ascenso Mid", "mid", "none", 2001, "template", Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "mid");
        add("late_upgrade_template", "Plantilla de Ascenso Late", "late", "none", 2002, "template", Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, "late");

        add("temporal_core", "Nucleo Temporal", "mid", "tiempo_rojo", 2010, "material", Material.CLOCK, "mid");
        add("red_time_ingot", "Lingote de Tiempo Rojo", "mid", "tiempo_rojo", 2011, "material", Material.COPPER_INGOT, "mid");
        add("stalker_feather", "Pluma del Acechador", "mid", "arquero", 2012, "material", Material.PHANTOM_MEMBRANE, "mid");
        add("archer_core", "Nucleo de Arquero", "mid", "arquero", 2013, "material", Material.FLINT, "mid");
        add("light_alloy", "Aleacion Liviana", "mid", "metal_ligero", 2014, "material", Material.IRON_INGOT, "mid");
        add("crystal_fragment", "Fragmento Cristalizado", "mid", "acero_cristalizado", 2015, "material", Material.AMETHYST_SHARD, "mid");
        add("crystallized_steel", "Acero Cristalizado", "mid", "acero_cristalizado", 2016, "material", Material.IRON_INGOT, "mid");

        add("darkness_fragment", "Fragmento de Oscuridad", "late", "diamante_oscuro", 2020, "material", Material.ECHO_SHARD, "late");
        add("dark_diamond_core", "Nucleo de Diamante Oscuro", "late", "diamante_oscuro", 2021, "material", Material.NETHERITE_INGOT, "late");
        add("eel_heart", "Corazon de Anguila", "late", "oceano_electrico", 2022, "material", Material.HEART_OF_THE_SEA, "late");
        add("storm_pearl", "Perla Tormentosa", "late", "oceano_electrico", 2023, "material", Material.ENDER_PEARL, "late");
        add("electric_ocean_core", "Nucleo de Oceano Electrico", "late", "oceano_electrico", 2024, "material", Material.CONDUIT, "late");
        add("perfect_crimson_flower", "Flor Carmesi Perfecta", "late", "calista_sangre", 2025, "plant_product", Material.NETHER_WART, "late");
        add("crimson_heart", "Corazon Carmesi", "late", "calista_sangre", 2026, "material", Material.NETHER_WART, "late");
        add("blood_calista_core", "Nucleo de Calista de Sangre", "late", "calista_sangre", 2027, "material", Material.NETHER_WART_BLOCK, "late");
        add("infernal_ice", "Hielo Infernal", "late", "nieve_abajo", 2028, "material", Material.BLUE_ICE, "late");
        add("black_frost", "Escarcha Negra", "late", "nieve_abajo", 2029, "plant_product", Material.BLACK_DYE, "late");
        add("below_snow_core", "Nucleo de Nieve de Abajo", "late", "nieve_abajo", 2030, "material", Material.BLUE_ICE, "late");
        add("ancient_kelp", "Alga Ancestral", "pesca", "oceano_electrico", 2031, "material", Material.KELP, "late");

        addPlant("time_flower_seed", "Semilla de Flor del Tiempo", 2100);
        addPlant("crimson_flower_seed", "Semilla de Flor Carmesi", 2101);
        addPlant("black_frost_seed", "Semilla de Escarcha Negra", 2102);
        addPlant("glow_moss_seed", "Semilla de Musgo Luminoso", 2103);
        addPlant("ancient_kelp_seed", "Semilla de Alga Ancestral", 2104);
        addPlant("living_coral_seed", "Semilla de Coral Vivo", 2105);
        addPlant("blue_snow_flower_seed", "Semilla de Flor de Nieve Azul", 2106);
        addPlant("amber_fungus_spore", "Espora de Hongo Ambar", 2107);
        addPlant("basalt_root_seed", "Semilla de Raiz de Basalto", 2108);
        addPlant("honey_sprout_seed", "Semilla de Brote de Miel", 2109);
        addPlant("prism_petal_seed", "Semilla de Petalo Prismático", 2110);

        add("time_flower", "Flor del Tiempo", "plantas", "none", 2120, "plant_product", Material.POPPY, null);
        add("glow_moss", "Musgo Luminoso", "plantas", "none", 2121, "plant_product", Material.MOSS_BLOCK, null);
        add("living_coral", "Coral Vivo", "plantas", "none", 2122, "plant_product", Material.BRAIN_CORAL, null);
        add("blue_snow_flower", "Flor de Nieve Azul", "plantas", "none", 2123, "plant_product", Material.BLUE_ORCHID, null);
        add("amber_fungus", "Hongo Ambar", "plantas", "none", 2124, "plant_product", Material.BROWN_MUSHROOM, null);
        add("basalt_root", "Raiz de Basalto", "plantas", "none", 2125, "plant_product", Material.BASALT, null);
        add("honey_sprout", "Brote de Miel", "plantas", "none", 2126, "plant_product", Material.HONEYCOMB, null);
        add("prism_petal", "Petalo Prismático", "plantas", "none", 2127, "plant_product", Material.AMETHYST_SHARD, null);
    }

    private void addPlant(String id, String name, int model) {
        add(id, name, "plantas", "none", model, "seed", Material.WHEAT_SEEDS, null);
    }
}
