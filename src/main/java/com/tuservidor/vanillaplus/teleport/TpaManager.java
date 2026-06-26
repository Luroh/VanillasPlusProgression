package com.tuservidor.vanillaplus.teleport;

import com.tuservidor.vanillaplus.config.ConfigManager;
import com.tuservidor.vanillaplus.config.MessageManager;
import com.tuservidor.vanillaplus.recipe.RecipeCategory;
import com.tuservidor.vanillaplus.unlock.UnlockManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TpaManager implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final MessageManager messages;
    private final UnlockManager unlockManager;
    private final Map<UUID, Request> incoming = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, ActiveTeleport> active = new HashMap<>();

    public TpaManager(JavaPlugin plugin, ConfigManager config, MessageManager messages, UnlockManager unlockManager) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.unlockManager = unlockManager;
    }

    public boolean utilsUnlocked(Player player) {
        if (!unlockManager.isUnlocked(RecipeCategory.UTILS)) {
            messages.send(player, "utils_locked");
            return false;
        }
        return true;
    }

    public void request(Player sender, Player target) {
        if (!utilsUnlocked(sender)) return;
        if (sender.equals(target)) {
            sender.sendMessage("§cNo puedes enviarte TPA a ti mismo.");
            return;
        }
        long now = System.currentTimeMillis();
        long cdUntil = cooldowns.getOrDefault(sender.getUniqueId(), 0L);
        if (cdUntil > now) {
            sender.sendMessage("§cEspera " + ((cdUntil - now) / 1000 + 1) + "s para volver a usar /tpa.");
            return;
        }
        int requestSeconds = config.main().getInt("teleport.tpa_request_seconds", 60);
        incoming.put(target.getUniqueId(), new Request(sender.getUniqueId(), target.getUniqueId(), now + requestSeconds * 1000L));
        cooldowns.put(sender.getUniqueId(), now + config.main().getInt("teleport.tpa_cooldown_seconds", 60) * 1000L);
        messages.send(sender, "tpa_sent", Map.of("target", target.getName()));
        messages.send(target, "tpa_received", Map.of("player", sender.getName()));
    }

    public void accept(Player target) {
        if (!utilsUnlocked(target)) return;
        Request request = incoming.remove(target.getUniqueId());
        if (request == null || request.expiresAt < System.currentTimeMillis()) {
            messages.send(target, "tpa_none");
            return;
        }
        Player sender = Bukkit.getPlayer(request.sender);
        if (sender == null || !sender.isOnline()) {
            target.sendMessage("§cEl jugador ya no esta conectado.");
            return;
        }
        startTeleport(sender, target);
    }

    public void deny(Player target) {
        Request request = incoming.remove(target.getUniqueId());
        if (request == null) messages.send(target, "tpa_none");
        else messages.send(target, "tpa_denied");
    }

    private void startTeleport(Player mover, Player target) {
        cancelActive(mover.getUniqueId(), null);
        int seconds = config.main().getInt("teleport.warmup_seconds", 5);
        ActiveTeleport tele = new ActiveTeleport(mover.getUniqueId(), target.getUniqueId(), mover.getLocation().clone(), target.getLocation().clone(), null);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            active.remove(mover.getUniqueId());
            Player m = Bukkit.getPlayer(tele.mover);
            Player t = Bukkit.getPlayer(tele.target);
            if (m == null || t == null || !m.isOnline() || !t.isOnline()) return;
            m.teleport(t.getLocation());
            messages.send(m, "teleport_done");
        }, seconds * 20L);
        tele.task = task;
        active.put(mover.getUniqueId(), tele);
        mover.sendMessage("§eTPA aceptado. Teletransporte en " + seconds + " segundos.");
        target.sendMessage("§eTPA aceptado. No se muevan ni reciban dano.");
    }

    private void cancelActive(UUID mover, String reason) {
        ActiveTeleport teleport = active.remove(mover);
        if (teleport == null) return;
        if (teleport.task != null) teleport.task.cancel();
        Player m = Bukkit.getPlayer(teleport.mover);
        Player t = Bukkit.getPlayer(teleport.target);
        if (reason != null) {
            if (m != null) messages.send(m, reason);
            if (t != null) messages.send(t, reason);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!config.main().getBoolean("teleport.cancel_on_move_block", true)) return;
        UUID moved = event.getPlayer().getUniqueId();
        for (ActiveTeleport tele : new ArrayList<>(active.values())) {
            Location start = moved.equals(tele.mover) ? tele.moverStart : moved.equals(tele.target) ? tele.targetStart : null;
            if (start == null || event.getTo() == null) continue;
            Location to = event.getTo();
            if (start.getBlockX() != to.getBlockX() || start.getBlockY() != to.getBlockY() || start.getBlockZ() != to.getBlockZ()) {
                cancelActive(tele.mover, "teleport_cancelled_move");
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!config.main().getBoolean("teleport.cancel_on_damage", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        UUID damaged = player.getUniqueId();
        for (ActiveTeleport tele : new ArrayList<>(active.values())) {
            if (damaged.equals(tele.mover) || damaged.equals(tele.target)) cancelActive(tele.mover, "teleport_cancelled_damage");
        }
    }

    public void shutdown() {
        for (ActiveTeleport tele : new ArrayList<>(active.values())) cancelActive(tele.mover, null);
    }

    private static class Request {
        private final UUID sender;
        private final UUID target;
        private final long expiresAt;
        private Request(UUID sender, UUID target, long expiresAt) { this.sender = sender; this.target = target; this.expiresAt = expiresAt; }
    }

    private static class ActiveTeleport {
        private final UUID mover;
        private final UUID target;
        private final Location moverStart;
        private final Location targetStart;
        private BukkitTask task;
        private ActiveTeleport(UUID mover, UUID target, Location moverStart, Location targetStart, BukkitTask task) {
            this.mover = mover; this.target = target; this.moverStart = moverStart; this.targetStart = targetStart; this.task = task;
        }
    }
}
