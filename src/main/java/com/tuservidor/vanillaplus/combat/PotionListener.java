package com.tuservidor.vanillaplus.combat;

import com.tuservidor.vanillaplus.set.SetDefinition;
import com.tuservidor.vanillaplus.set.SetManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PotionListener implements Listener {
    private final Set<PotionEffectType> negative = new HashSet<>();
    private final Set<UUID> applying = new HashSet<>();
    private final JavaPlugin plugin;
    private final SetManager setManager;

    public PotionListener(JavaPlugin plugin, SetManager setManager) {
        this.plugin = plugin;
        this.setManager = setManager;
        negative.add(PotionEffectType.POISON);
        negative.add(PotionEffectType.WEAKNESS);
        negative.add(PotionEffectType.SLOWNESS);
        negative.add(PotionEffectType.WITHER);
        negative.add(PotionEffectType.BLINDNESS);
        negative.add(PotionEffectType.DARKNESS);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        SetDefinition armor = setManager.getDefinition(setManager.getState(player).armorSetId);
        if (armor == null) return;
        EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();
        if (reason == EntityRegainHealthEvent.RegainReason.MAGIC
                || reason == EntityRegainHealthEvent.RegainReason.MAGIC_REGEN
                || reason == EntityRegainHealthEvent.RegainReason.EATING
                || reason == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setAmount(event.getAmount() * armor.instantPotionHealMultiplier);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (applying.contains(player.getUniqueId())) return;
        PotionEffect effect = event.getNewEffect();
        if (effect == null || effect.getDuration() <= 1) return;
        SetDefinition armor = setManager.getDefinition(setManager.getState(player).armorSetId);
        if (armor == null) return;
        boolean isNegative = negative.contains(effect.getType());
        double mult = isNegative ? armor.negativePotionDurationMultiplier : armor.positivePotionDurationMultiplier;
        if (Math.abs(mult - 1.0) < 0.001) return;
        int newDuration = Math.max(1, (int) Math.round(effect.getDuration() * mult));
        PotionEffect modified = new PotionEffect(effect.getType(), newDuration, effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon());
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            applying.add(player.getUniqueId());
            try {
                player.addPotionEffect(modified, true);
            } finally {
                applying.remove(player.getUniqueId());
            }
        });
    }
}
