package com.tuservidor.vanillaplus.set;

import com.tuservidor.vanillaplus.config.ConfigManager;
import com.tuservidor.vanillaplus.item.ItemFactory;
import com.tuservidor.vanillaplus.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SetManager implements Listener {
    private static final float DEFAULT_WALK_SPEED = 0.2f;
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final ItemFactory itemFactory;
    private final Map<String, SetDefinition> definitions = new HashMap<>();
    private final Map<UUID, PlayerSetState> cache = new HashMap<>();

    public SetManager(JavaPlugin plugin, ConfigManager config, ItemFactory itemFactory) {
        this.plugin = plugin;
        this.config = config;
        this.itemFactory = itemFactory;
        loadDefinitions();
    }

    public void loadDefinitions() {
        definitions.clear();
        ConfigurationSection section = config.sets().getConfigurationSection("sets");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection setSection = section.getConfigurationSection(id);
            if (setSection != null) definitions.put(id, new SetDefinition(id, setSection));
        }
    }

    public SetDefinition getDefinition(String setId) {
        return setId == null ? null : definitions.get(setId);
    }

    public PlayerSetState getState(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(), PlayerSetState::new);
    }

    public void recalculate(Player player) {
        PlayerSetState state = cache.computeIfAbsent(player.getUniqueId(), PlayerSetState::new);
        state.armorSetId = detectArmorSet(player);
        state.swordSetId = detectHeld(player, "sword");
        state.rangedSetId = detectRanged(player);
        state.amuletSetId = detectAmulet(player);
        SetDefinition def = getDefinition(state.armorSetId);
        state.tier = def == null ? null : def.tier;
        state.lastUpdated = System.currentTimeMillis();
        applyMovement(player, def);
    }

    private String detectArmorSet(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        String found = null;
        for (ItemStack piece : armor) {
            if (ItemUtil.isEmpty(piece)) return null;
            if (!"armor".equals(itemFactory.getItemType(piece))) return null;
            String set = itemFactory.getSetId(piece);
            if (set == null) return null;
            if (found == null) found = set;
            else if (!found.equals(set)) return null;
        }
        return found;
    }

    private String detectHeld(Player player, String type) {
        ItemStack main = player.getInventory().getItemInMainHand();
        return type.equals(itemFactory.getItemType(main)) ? itemFactory.getSetId(main) : null;
    }

    private String detectRanged(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        String type = itemFactory.getItemType(main);
        if ("bow".equals(type) || "crossbow".equals(type)) return itemFactory.getSetId(main);
        ItemStack off = player.getInventory().getItemInOffHand();
        type = itemFactory.getItemType(off);
        if ("bow".equals(type) || "crossbow".equals(type)) return itemFactory.getSetId(off);
        return null;
    }

    private String detectAmulet(Player player) {
        boolean requireOffhand = config.main().getBoolean("amulets.require_offhand", true);
        if (requireOffhand) {
            ItemStack off = player.getInventory().getItemInOffHand();
            return "amulet".equals(itemFactory.getItemType(off)) ? itemFactory.getSetId(off) : null;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if ("amulet".equals(itemFactory.getItemType(item))) return itemFactory.getSetId(item);
        }
        return null;
    }

    private void applyMovement(Player player, SetDefinition armorDef) {
        double bonus = armorDef == null ? 0.0 : armorDef.movementSpeed;
        Material bootsBelow = player.getLocation().clone().add(0, -1, 0).getBlock().getType();
        if ("nieve".equals(armorDef == null ? null : armorDef.id) && (bootsBelow == Material.ICE || bootsBelow == Material.PACKED_ICE || bootsBelow == Material.BLUE_ICE)) bonus += 0.15;
        if ("nieve_abajo".equals(armorDef == null ? null : armorDef.id) && (bootsBelow == Material.SOUL_SAND || bootsBelow == Material.SOUL_SOIL || bootsBelow == Material.BASALT || bootsBelow == Material.BLACKSTONE)) bonus += 0.18;
        if ("oceano_electrico".equals(armorDef == null ? null : armorDef.id) && (player.isInWater() || player.getWorld().hasStorm())) bonus += 0.30;
        float speed = (float) Math.max(0.05, Math.min(0.6, DEFAULT_WALK_SPEED * (1.0 + bonus)));
        player.setWalkSpeed(speed);
    }

    public void reset(Player player) {
        cache.remove(player.getUniqueId());
        try { player.setWalkSpeed(DEFAULT_WALK_SPEED); } catch (Exception ignored) {}
    }

    public void shutdown() {
        for (UUID uuid : new ArrayList<>(cache.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) reset(player);
        }
    }

    private void delayedRecalc(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> recalculate(player), 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) { delayedRecalc(event.getPlayer()); }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { reset(event.getPlayer()); }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) { delayedRecalc(event.getPlayer()); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) { if (event.getWhoClicked() instanceof Player p) delayedRecalc(p); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) { if (event.getWhoClicked() instanceof Player p) delayedRecalc(p); }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent event) { delayedRecalc(event.getPlayer()); }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) { delayedRecalc(event.getPlayer()); }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        PlayerSetState state = getState(event.getPlayer());
        if (state.armorSetId == null) return;
        SetDefinition def = getDefinition(state.armorSetId);
        if (def == null) return;
        if ("nieve".equals(def.id) || "nieve_abajo".equals(def.id) || "oceano_electrico".equals(def.id)) applyMovement(event.getPlayer(), def);
    }
}
