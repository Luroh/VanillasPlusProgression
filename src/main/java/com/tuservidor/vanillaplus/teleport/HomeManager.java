package com.tuservidor.vanillaplus.teleport;

import com.tuservidor.vanillaplus.config.ConfigManager;
import com.tuservidor.vanillaplus.config.MessageManager;
import com.tuservidor.vanillaplus.data.DataStore;
import com.tuservidor.vanillaplus.item.CustomItemIds;
import com.tuservidor.vanillaplus.item.ItemFactory;
import com.tuservidor.vanillaplus.recipe.RecipeCategory;
import com.tuservidor.vanillaplus.unlock.UnlockManager;
import com.tuservidor.vanillaplus.util.ItemUtil;
import com.tuservidor.vanillaplus.util.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class HomeManager implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final MessageManager messages;
    private final UnlockManager unlockManager;
    private final ItemFactory itemFactory;
    private final DataStore players;
    private final Map<UUID, Warmup> warmups = new HashMap<>();

    public HomeManager(JavaPlugin plugin, ConfigManager config, MessageManager messages, UnlockManager unlockManager, ItemFactory itemFactory) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.unlockManager = unlockManager;
        this.itemFactory = itemFactory;
        this.players = new DataStore(plugin, "data/players.yml");
    }

    public boolean utilsUnlocked(Player player) {
        if (!unlockManager.isUnlocked(RecipeCategory.UTILS)) {
            messages.send(player, "utils_locked");
            return false;
        }
        return true;
    }

    public int getLimit(UUID uuid) {
        String path = "players." + uuid + ".home_limit";
        if (!players.get().contains(path)) {
            players.get().set(path, config.main().getInt("homes.default_limit", 1));
            players.save();
        }
        return players.get().getInt(path, config.main().getInt("homes.default_limit", 1));
    }

    public void setLimit(UUID uuid, int limit) {
        int max = config.main().getInt("homes.max_limit", 10);
        players.get().set("players." + uuid + ".home_limit", Math.min(limit, max));
        players.save();
    }

    public int countHomes(UUID uuid) {
        ConfigurationSection homes = players.get().getConfigurationSection("players." + uuid + ".homes");
        return homes == null ? 0 : homes.getKeys(false).size();
    }

    public Set<String> listHomes(UUID uuid) {
        ConfigurationSection homes = players.get().getConfigurationSection("players." + uuid + ".homes");
        return homes == null ? Set.of() : new TreeSet<>(homes.getKeys(false));
    }

    public void setHome(Player player, String name) {
        name = normalizeHomeName(name);
        UUID uuid = player.getUniqueId();
        boolean exists = players.get().contains("players." + uuid + ".homes." + name);
        if (!exists && countHomes(uuid) >= getLimit(uuid)) {
            messages.send(player, "home_limit");
            return;
        }
        ConfigurationSection section = players.get().createSection("players." + uuid + ".homes." + name);
        LocationSerializer.write(section, player.getLocation());
        players.save();
        messages.send(player, "home_set", Map.of("home", name));
    }

    public void delHome(Player player, String name) {
        name = normalizeHomeName(name);
        String path = "players." + player.getUniqueId() + ".homes." + name;
        if (!players.get().contains(path)) {
            messages.send(player, "home_missing");
            return;
        }
        players.get().set(path, null);
        players.save();
        messages.send(player, "home_deleted", Map.of("home", name));
    }

    public void teleportHome(Player player, String name) {
        name = normalizeHomeName(name);
        Location location = LocationSerializer.read(players.get().getConfigurationSection("players." + player.getUniqueId() + ".homes." + name));
        if (location == null) {
            messages.send(player, "home_missing");
            return;
        }
        startWarmup(player, location);
    }

    public void startWarmup(Player player, Location destination) {
        cancelWarmup(player.getUniqueId(), null);
        int seconds = config.main().getInt("teleport.warmup_seconds", 5);
        Location start = player.getLocation().clone();
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Warmup warmup = warmups.remove(player.getUniqueId());
            if (warmup == null) return;
            if (!player.isOnline()) return;
            Location safe = safeDestination(destination);
            if (safe == null) {
                player.sendMessage("§cDestino inseguro.");
                return;
            }
            player.teleport(safe);
            messages.send(player, "teleport_done");
        }, seconds * 20L);
        warmups.put(player.getUniqueId(), new Warmup(start, task));
        player.sendMessage("§eTeletransporte en " + seconds + " segundos. No te muevas ni recibas dano.");
    }

    private Location safeDestination(Location location) {
        if (location == null || location.getWorld() == null) return null;
        Location loc = location.clone();
        Material feet = loc.getBlock().getType();
        Material head = loc.clone().add(0, 1, 0).getBlock().getType();
        Material below = loc.clone().add(0, -1, 0).getBlock().getType();
        if (!feet.isAir() || !head.isAir()) loc = loc.getWorld().getHighestBlockAt(loc).getLocation().add(0.5, 1, 0.5);
        if (below == Material.LAVA || below == Material.FIRE || below == Material.CAMPFIRE || below == Material.SOUL_CAMPFIRE) return null;
        return loc;
    }

    public void cancelWarmup(UUID uuid, String messageKey) {
        Warmup warmup = warmups.remove(uuid);
        if (warmup == null) return;
        warmup.task.cancel();
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && messageKey != null) messages.send(player, messageKey);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!config.main().getBoolean("teleport.cancel_on_move_block", true)) return;
        Warmup warmup = warmups.get(event.getPlayer().getUniqueId());
        if (warmup == null) return;
        Location from = warmup.start;
        Location to = event.getTo();
        if (to == null) return;
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            cancelWarmup(event.getPlayer().getUniqueId(), "teleport_cancelled_move");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!config.main().getBoolean("teleport.cancel_on_damage", true)) return;
        if (event.getEntity() instanceof Player player) cancelWarmup(player.getUniqueId(), "teleport_cancelled_damage");
    }

    @EventHandler(ignoreCancelled = true)
    public void onHomeCrystal(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!itemFactory.isCustom(item, CustomItemIds.HOME_CRYSTAL)) return;
        event.setCancelled(true);
        if (!utilsUnlocked(player)) return;
        int current = getLimit(player.getUniqueId());
        int inc = config.main().getInt("homes.home_crystal_increment", 1);
        int max = config.main().getInt("homes.max_limit", 10);
        if (current >= max) {
            player.sendMessage("§cYa tienes el limite maximo de homes.");
            return;
        }
        setLimit(player.getUniqueId(), current + inc);
        ItemUtil.consumeOne(item);
        messages.send(player, "home_crystal_used", Map.of("limit", String.valueOf(getLimit(player.getUniqueId()))));
    }

    public void save() { players.save(); }

    private String normalizeHomeName(String name) {
        if (name == null || name.isBlank()) return "home";
        String clean = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "");
        if (clean.isBlank()) return "home";
        return clean.substring(0, Math.min(24, clean.length()));
    }

    private record Warmup(Location start, BukkitTask task) {}
}
