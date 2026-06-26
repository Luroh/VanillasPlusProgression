package com.tuservidor.vanillaplus.recipe;

import com.tuservidor.vanillaplus.item.ItemFactory;
import com.tuservidor.vanillaplus.item.ItemKeys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class RecipeRegistry {
    private final JavaPlugin plugin;
    private final ItemFactory itemFactory;
    private final Map<NamespacedKey, RecipeCategory> categories = new HashMap<>();
    private final Map<RecipeCategory, List<NamespacedKey>> byCategory = new EnumMap<>(RecipeCategory.class);

    public RecipeRegistry(JavaPlugin plugin, ItemFactory itemFactory) {
        this.plugin = plugin;
        this.itemFactory = itemFactory;
        for (RecipeCategory category : RecipeCategory.values()) byCategory.put(category, new ArrayList<>());
    }

    public void registerAll() {
        registerEarlySet("pluma", Material.FEATHER, false);
        registerEarlySet("musgo", Material.MOSS_BLOCK, false);
        registerEarlySet("nieve", Material.SNOW_BLOCK, true);
        registerEarlySet("miel", Material.HONEYCOMB, false);
        registerTemplates();
        registerMidMaterials();
        registerLateCores();
        registerSmithingLine("musgo", "tiempo_rojo", "red_time_ingot", false, RecipeCategory.MID);
        registerSmithingLine("miel", "arquero", "archer_core", false, RecipeCategory.MID);
        registerSmithingLine("pluma", "metal_ligero", "light_alloy", false, RecipeCategory.MID);
        registerSmithingLine("nieve", "acero_cristalizado", "crystallized_steel", true, RecipeCategory.MID);
        registerSmithingLine("acero_cristalizado", "diamante_oscuro", "dark_diamond_core", true, RecipeCategory.LATE);
        registerSmithingLine("arquero", "oceano_electrico", "electric_ocean_core", false, RecipeCategory.LATE);
        registerSmithingLine("tiempo_rojo", "calista_sangre", "blood_calista_core", false, RecipeCategory.LATE);
        registerSmithingLine("metal_ligero", "nieve_abajo", "below_snow_core", false, RecipeCategory.LATE);
        registerHomeCrystal();
    }

    public RecipeCategory categoryOf(NamespacedKey key) { return categories.get(key); }
    public List<NamespacedKey> keysFor(RecipeCategory category) { return Collections.unmodifiableList(byCategory.getOrDefault(category, List.of())); }
    public Collection<NamespacedKey> allKeys() { return Collections.unmodifiableSet(categories.keySet()); }

    private void registerEarlySet(String set, Material material, boolean crossbow) {
        RecipeChoice x = new RecipeChoice.MaterialChoice(material);
        addArmorAndTools(set, x, RecipeCategory.EARLY, crossbow);
    }

    private void addArmorAndTools(String set, RecipeChoice x, RecipeCategory category, boolean crossbow) {
        shaped(set + "_helmet", itemFactory.create(set + "_helmet"), category, new String[]{"XXX", "X X"}, Map.of('X', x));
        shaped(set + "_chestplate", itemFactory.create(set + "_chestplate"), category, new String[]{"X X", "XXX", "XXX"}, Map.of('X', x));
        shaped(set + "_leggings", itemFactory.create(set + "_leggings"), category, new String[]{"XXX", "X X", "X X"}, Map.of('X', x));
        shaped(set + "_boots", itemFactory.create(set + "_boots"), category, new String[]{"X X", "X X"}, Map.of('X', x));
        shaped(set + "_sword", itemFactory.create(set + "_sword"), category, new String[]{" X ", " X ", " S "}, Map.of('X', x, 'S', new RecipeChoice.MaterialChoice(Material.STICK)));
        if (crossbow) {
            shaped(set + "_crossbow", itemFactory.create(set + "_crossbow"), category, new String[]{"TIT", "SXS", " T "}, Map.of(
                    'T', new RecipeChoice.MaterialChoice(Material.STICK),
                    'I', new RecipeChoice.MaterialChoice(Material.IRON_INGOT),
                    'S', new RecipeChoice.MaterialChoice(Material.STRING),
                    'X', x
            ));
        } else {
            shaped(set + "_bow", itemFactory.create(set + "_bow"), category, new String[]{" TS", "T X", " TS"}, Map.of(
                    'T', new RecipeChoice.MaterialChoice(Material.STICK),
                    'S', new RecipeChoice.MaterialChoice(Material.STRING),
                    'X', x
            ));
        }
        shaped(set + "_amulet", itemFactory.create(set + "_amulet"), category, new String[]{" X ", "XEX", " X "}, Map.of('X', x, 'E', new RecipeChoice.MaterialChoice(Material.EMERALD)));
    }

    private void registerTemplates() {
        shaped("mid_upgrade_template", itemFactory.create("mid_upgrade_template"), RecipeCategory.MID,
                new String[]{"CCC", "CNC", "CCC"}, Map.of('C', new RecipeChoice.MaterialChoice(Material.COPPER_INGOT), 'N', new RecipeChoice.MaterialChoice(Material.NETHERITE_SCRAP)));
        shaped("late_upgrade_template", itemFactory.create("late_upgrade_template"), RecipeCategory.LATE,
                new String[]{"DND", "NTN", "DND"}, Map.of(
                        'D', new RecipeChoice.MaterialChoice(Material.DIAMOND),
                        'N', new RecipeChoice.MaterialChoice(Material.NETHERITE_SCRAP),
                        'T', exact("mid_upgrade_template")
                ));
    }

    private void registerMidMaterials() {
        shaped("red_time_ingot", itemFactory.create("red_time_ingot"), RecipeCategory.MID,
                new String[]{"RCR", "CNC", "RCR"}, Map.of('R', new RecipeChoice.MaterialChoice(Material.BEETROOT), 'C', new RecipeChoice.MaterialChoice(Material.CLOCK), 'N', exact("temporal_core")));
        shaped("archer_core", itemFactory.create("archer_core"), RecipeCategory.MID,
                new String[]{"FCF", "CPC", "FCF"}, Map.of('F', new RecipeChoice.MaterialChoice(Material.FLINT), 'C', new RecipeChoice.MaterialChoice(Material.STRING), 'P', exact("stalker_feather")));
        shaped("light_alloy", itemFactory.create("light_alloy"), RecipeCategory.MID,
                new String[]{"ICI", "CNC", "IFI"}, Map.of('I', new RecipeChoice.MaterialChoice(Material.IRON_INGOT), 'C', new RecipeChoice.MaterialChoice(Material.COPPER_INGOT), 'N', new RecipeChoice.MaterialChoice(Material.NETHERITE_SCRAP), 'F', new RecipeChoice.MaterialChoice(Material.FEATHER)));
        shaped("crystallized_steel", itemFactory.create("crystallized_steel"), RecipeCategory.MID,
                new String[]{"AIA", "IFI", "AIA"}, Map.of('A', new RecipeChoice.MaterialChoice(Material.AMETHYST_SHARD), 'I', new RecipeChoice.MaterialChoice(Material.IRON_INGOT), 'F', exact("crystal_fragment")));
    }

    private void registerLateCores() {
        shaped("dark_diamond_core", itemFactory.create("dark_diamond_core"), RecipeCategory.LATE,
                new String[]{"DOD", "ONO", "DOD"}, Map.of('D', new RecipeChoice.MaterialChoice(Material.DIAMOND), 'O', exact("darkness_fragment"), 'N', new RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT)));
        shaped("electric_ocean_core", itemFactory.create("electric_ocean_core"), RecipeCategory.LATE,
                new String[]{"LTL", "EAE", "LTL"}, Map.of('L', new RecipeChoice.MaterialChoice(Material.LAPIS_LAZULI), 'T', exact("storm_pearl"), 'E', exact("eel_heart"), 'A', exact("ancient_kelp")));
        shaped("blood_calista_core", itemFactory.create("blood_calista_core"), RecipeCategory.LATE,
                new String[]{"BCB", "CFC", "BNB"}, Map.of('B', new RecipeChoice.MaterialChoice(Material.BEETROOT), 'C', exact("crimson_heart"), 'F', exact("perfect_crimson_flower"), 'N', new RecipeChoice.MaterialChoice(Material.NETHER_WART_BLOCK)));
        shaped("below_snow_core", itemFactory.create("below_snow_core"), RecipeCategory.LATE,
                new String[]{"BHB", "HSH", "BHB"}, Map.of('B', new RecipeChoice.MaterialChoice(Material.BLUE_ICE), 'H', exact("infernal_ice"), 'S', exact("black_frost")));
    }

    private void registerSmithingLine(String baseSet, String resultSet, String additionId, boolean crossbow, RecipeCategory category) {
        String template = category == RecipeCategory.MID ? "mid_upgrade_template" : "late_upgrade_template";
        registerSmith(resultSet + "_helmet", baseSet + "_helmet", additionId, template, category);
        registerSmith(resultSet + "_chestplate", baseSet + "_chestplate", additionId, template, category);
        registerSmith(resultSet + "_leggings", baseSet + "_leggings", additionId, template, category);
        registerSmith(resultSet + "_boots", baseSet + "_boots", additionId, template, category);
        registerSmith(resultSet + "_sword", baseSet + "_sword", additionId, template, category);
        if (crossbow) registerSmith(resultSet + "_crossbow", baseSet + "_crossbow", additionId, template, category);
        else registerSmith(resultSet + "_bow", baseSet + "_bow", additionId, template, category);
        registerSmith(resultSet + "_amulet", baseSet + "_amulet", additionId, template, category);
    }

    private void registerSmith(String resultId, String baseId, String additionId, String templateId, RecipeCategory category) {
        NamespacedKey key = ItemKeys.recipe(resultId);
        plugin.getServer().removeRecipe(key);
        Recipe recipe = new SmithingTransformRecipe(key, itemFactory.create(resultId), exact(templateId), exact(baseId), exact(additionId));
        plugin.getServer().addRecipe(recipe);
        track(key, category);
    }

    private void registerHomeCrystal() {
        shaped("home_crystal", itemFactory.create("home_crystal"), RecipeCategory.UTILS,
                new String[]{"I I", " D ", "I I"}, Map.of('I', new RecipeChoice.MaterialChoice(Material.IRON_NUGGET), 'D', new RecipeChoice.MaterialChoice(Material.DIAMOND_BLOCK)));
    }

    private RecipeChoice.ExactChoice exact(String itemId) {
        return new RecipeChoice.ExactChoice(itemFactory.create(itemId));
    }

    private void shaped(String id, ItemStack result, RecipeCategory category, String[] shape, Map<Character, RecipeChoice> choices) {
        NamespacedKey key = ItemKeys.recipe(id);
        plugin.getServer().removeRecipe(key);
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        recipe.shape(shape);
        for (Map.Entry<Character, RecipeChoice> entry : choices.entrySet()) {
            recipe.setIngredient(entry.getKey(), entry.getValue());
        }
        plugin.getServer().addRecipe(recipe);
        track(key, category);
    }

    private void track(NamespacedKey key, RecipeCategory category) {
        categories.put(key, category);
        byCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(key);
    }
}
