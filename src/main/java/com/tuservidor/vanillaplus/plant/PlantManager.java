package com.tuservidor.vanillaplus.plant;

import com.tuservidor.vanillaplus.config.ConfigManager;
import com.tuservidor.vanillaplus.data.DataStore;
import com.tuservidor.vanillaplus.item.ItemFactory;
import com.tuservidor.vanillaplus.recipe.RecipeCategory;
import com.tuservidor.vanillaplus.unlock.UnlockManager;
import com.tuservidor.vanillaplus.util.ItemUtil;
import com.tuservidor.vanillaplus.util.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class PlantManager implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final UnlockManager unlockManager;
    private final ItemFactory itemFactory;
    private final DataStore store;
    private final Map<String, PlantDefinition> definitions = new HashMap<>();
    private final Random random = new Random();
    private int saveTask = -1;

    public PlantManager(JavaPlugin plugin, ConfigManager config, UnlockManager unlockManager, ItemFactory itemFactory) {
        this.plugin = plugin;
        this.config = config;
        this.unlockManager = unlockManager;
        this.itemFactory = itemFactory;
        this.store = new DataStore(plugin, "data/plants.yml");
        registerDefinitions();
    }

    public void start() {
        saveTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::growRegisteredPlants, 20L * 60L, 20L * 60L);
    }

    public void shutdown() {
        if (saveTask != -1) Bukkit.getScheduler().cancelTask(saveTask);
        store.save();
    }

    private void registerDefinitions() {
        add(new PlantDefinition("time_flower_seed", "time_flower", "Flor del Tiempo", Material.BEETROOTS, 4, false, Material.FARMLAND));
        add(new PlantDefinition("crimson_flower_seed", "perfect_crimson_flower", "Flor Carmesi", Material.NETHER_WART, 4, true, Material.SOUL_SAND, Material.NETHER_WART_BLOCK));
        add(new PlantDefinition("black_frost_seed", "black_frost", "Escarcha Negra", Material.NETHER_WART, 5, true, Material.SOUL_SOIL));
        add(new PlantDefinition("glow_moss_seed", "glow_moss", "Musgo Luminoso", Material.MOSS_CARPET, 3, false, Material.MOSS_BLOCK));
        add(new PlantDefinition("ancient_kelp_seed", "ancient_kelp", "Alga Ancestral", Material.KELP, 4, false, Material.SAND, Material.GRAVEL));
        add(new PlantDefinition("living_coral_seed", "living_coral", "Coral Vivo", Material.BRAIN_CORAL, 3, false, Material.BRAIN_CORAL_BLOCK, Material.BUBBLE_CORAL_BLOCK, Material.FIRE_CORAL_BLOCK, Material.HORN_CORAL_BLOCK, Material.TUBE_CORAL_BLOCK));
        add(new PlantDefinition("blue_snow_flower_seed", "blue_snow_flower", "Flor de Nieve Azul", Material.BLUE_ORCHID, 3, false, Material.SNOW_BLOCK));
        add(new PlantDefinition("amber_fungus_spore", "amber_fungus", "Hongo Ambar", Material.BROWN_MUSHROOM, 3, false, Material.MYCELIUM, Material.PODZOL));
        add(new PlantDefinition("basalt_root_seed", "basalt_root", "Raiz de Basalto", Material.CRIMSON_ROOTS, 3, false, Material.BASALT));
        add(new PlantDefinition("honey_sprout_seed", "honey_sprout", "Brote de Miel", Material.WHEAT, 3, false, Material.FARMLAND));
        add(new PlantDefinition("prism_petal_seed", "prism_petal", "Petalo Prismático", Material.AMETHYST_CLUSTER, 4, false, Material.AMETHYST_BLOCK));
    }

    private void add(PlantDefinition definition) { definitions.put(definition.seedId, definition); }

    @EventHandler(ignoreCancelled = true)
    public void onPlant(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!unlockManager.isUnlocked(RecipeCategory.PLANTAS)) return;
        ItemStack item = event.getItem();
        String seedId = itemFactory.getItemId(item);
        PlantDefinition definition = definitions.get(seedId);
        if (definition == null) return;
        Block base = event.getClickedBlock();
        if (base == null || !definition.isValidBase(base.getType())) {
            event.getPlayer().sendMessage("§cNo puedes plantar eso en este bloque.");
            return;
        }
        if (!validBiome(event.getPlayer(), definition)) {
            event.getPlayer().sendMessage("§cLa condicion/bioma no sirve para esta planta.");
            return;
        }
        Block target = base.getRelative(0, 1, 0);
        if (!target.getType().isAir() && target.getType() != Material.WATER) {
            event.getPlayer().sendMessage("§cNo hay espacio para plantar.");
            return;
        }
        event.setCancelled(true);
        target.setType(definition.visual);
        applyAge(target, 0);
        savePlant(target.getLocation(), definition, 0, event.getPlayer().getUniqueId());
        ItemUtil.consumeOne(item);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHarvest(BlockBreakEvent event) {
        if (!unlockManager.isUnlocked(RecipeCategory.PLANTAS)) return;
        String key = LocationSerializer.blockKey(event.getBlock().getLocation());
        ConfigurationSection section = store.get().getConfigurationSection("plants." + key);
        if (section != null) {
            event.setDropItems(false);
            PlantDefinition def = definitions.get(section.getString("plant_id"));
            int stage = section.getInt("stage", 0);
            if (def != null && stage >= def.maxStage) {
                ItemStack product = itemFactory.create(def.productId);
                product.setAmount(def.legendary ? 1 : 1 + random.nextInt(2));
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), product);
                if ("crimson_flower_seed".equals(def.seedId) && random.nextDouble() < 0.08) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("perfect_crimson_flower"));
                }
            } else if (def != null) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create(def.seedId));
            }
            store.get().set("plants." + key, null);
            store.save();
            return;
        }

        maybeDropSeed(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBoneMeal(PlayerInteractEvent event) {
        if (!unlockManager.isUnlocked(RecipeCategory.PLANTAS)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        if (event.getItem() == null || event.getItem().getType() != Material.BONE_MEAL) return;
        String key = LocationSerializer.blockKey(event.getClickedBlock().getLocation());
        ConfigurationSection section = store.get().getConfigurationSection("plants." + key);
        if (section == null) return;
        PlantDefinition def = definitions.get(section.getString("plant_id"));
        if (def == null) return;
        event.setCancelled(true);
        if (def.legendary && random.nextDouble() < 0.65) {
            event.getPlayer().sendMessage("§eLa planta legendaria resiste el bone meal.");
            ItemUtil.consumeOne(event.getItem());
            return;
        }
        int stage = Math.min(def.maxStage, section.getInt("stage", 0) + 1);
        section.set("stage", stage);
        applyAge(event.getClickedBlock(), stage);
        ItemUtil.consumeOne(event.getItem());
        store.save();
    }

    private void maybeDropSeed(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        if (type == Material.SHORT_GRASS || type == Material.TALL_GRASS || type == Material.WHEAT || type == Material.BEETROOTS) {
            if (random.nextDouble() < 0.06) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("time_flower_seed"));
            if (random.nextDouble() < 0.04) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("honey_sprout_seed"));
        }
        if (type == Material.NETHER_WART && random.nextDouble() < 0.04) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("crimson_flower_seed"));
        if ((type == Material.KELP || type == Material.SEAGRASS) && random.nextDouble() < 0.05) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("ancient_kelp_seed"));
        if (type == Material.MOSS_BLOCK && random.nextDouble() < 0.05) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("glow_moss_seed"));
        if (type == Material.BASALT && random.nextDouble() < 0.035) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("basalt_root_seed"));
        if (type == Material.AMETHYST_CLUSTER && random.nextDouble() < 0.04) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("prism_petal_seed"));
        if (type == Material.SNOW_BLOCK && random.nextDouble() < 0.04) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("blue_snow_flower_seed"));
        if ((type == Material.MYCELIUM || type == Material.PODZOL) && random.nextDouble() < 0.04) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemFactory.create("amber_fungus_spore"));
    }

    private boolean validBiome(Player player, PlantDefinition definition) {
        Biome biome = player.getLocation().getBlock().getBiome();
        String name = biome.name();
        if (definition.seedId.contains("crimson") && !name.contains("CRIMSON")) return false;
        if (definition.seedId.contains("black_frost") && !name.contains("SOUL_SAND_VALLEY")) return false;
        if (definition.seedId.contains("ancient_kelp") && !name.contains("OCEAN")) return false;
        if (definition.seedId.contains("living_coral") && !name.contains("WARM_OCEAN")) return false;
        return true;
    }

    private void savePlant(Location location, PlantDefinition definition, int stage, UUID player) {
        String key = LocationSerializer.blockKey(location);
        ConfigurationSection section = store.get().createSection("plants." + key);
        section.set("world", location.getWorld() == null ? "world" : location.getWorld().getName());
        section.set("x", location.getBlockX());
        section.set("y", location.getBlockY());
        section.set("z", location.getBlockZ());
        section.set("plant_id", definition.seedId);
        section.set("stage", stage);
        section.set("planted_by", player.toString());
        section.set("planted_at", System.currentTimeMillis());
        store.save();
    }

    private void growRegisteredPlants() {
        ConfigurationSection root = store.get().getConfigurationSection("plants");
        if (root == null) return;
        for (String key : new ArrayList<>(root.getKeys(false))) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) continue;
            String world = section.getString("world", "world");
            org.bukkit.World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) continue;
            Location loc = new Location(bukkitWorld, section.getInt("x"), section.getInt("y"), section.getInt("z"));
            PlantDefinition def = definitions.get(section.getString("plant_id"));
            if (def == null) continue;
            Block block = loc.getBlock();
            if (block.getType() != def.visual) {
                store.get().set("plants." + key, null);
                continue;
            }
            int stage = section.getInt("stage", 0);
            if (stage < def.maxStage && random.nextDouble() < (def.legendary ? 0.28 : 0.55)) {
                stage++;
                section.set("stage", stage);
                applyAge(block, stage);
            }
        }
        store.save();
    }

    private void applyAge(Block block, int stage) {
        if (block.getBlockData() instanceof Ageable ageable) {
            int max = ageable.getMaximumAge();
            ageable.setAge(Math.min(max, stage));
            block.setBlockData(ageable);
        }
    }
}
