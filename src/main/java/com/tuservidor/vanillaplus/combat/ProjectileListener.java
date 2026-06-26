package com.tuservidor.vanillaplus.combat;

import com.tuservidor.vanillaplus.item.ItemKeys;
import com.tuservidor.vanillaplus.set.PlayerSetState;
import com.tuservidor.vanillaplus.set.SetDefinition;
import com.tuservidor.vanillaplus.set.SetManager;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Random;

public class ProjectileListener implements Listener {
    private final SetManager setManager;
    private final Random random = new Random();

    public ProjectileListener(SetManager setManager) {
        this.setManager = setManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PlayerSetState state = setManager.getState(player);
        SetDefinition ranged = setManager.getDefinition(state.rangedSetId);
        if (ranged == null) return;
        double velocity = event.getBow() != null && event.getBow().getType().name().contains("CROSSBOW") ? ranged.crossbowProjectileVelocity : ranged.bowProjectileVelocity;
        if ("oceano_electrico".equals(ranged.id) && (player.isInWater() || player.getWorld().hasStorm())) velocity *= 1.05;
        Vector current = event.getProjectile().getVelocity();
        event.getProjectile().setVelocity(current.multiply(velocity));
        double damageMultiplier = 1.0;
        if ("arquero".equals(ranged.id)) damageMultiplier *= 1.08;
        if ("oceano_electrico".equals(ranged.id) && (player.isInWater() || player.getWorld().hasStorm())) damageMultiplier *= 1.10;
        event.getProjectile().getPersistentDataContainer().set(ItemKeys.PROJECTILE_DAMAGE_MULTIPLIER, PersistentDataType.DOUBLE, damageMultiplier);
        event.getProjectile().getPersistentDataContainer().set(ItemKeys.PROJECTILE_RANGED_SET, PersistentDataType.STRING, ranged.id);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        String rangedSet = arrow.getPersistentDataContainer().get(ItemKeys.PROJECTILE_RANGED_SET, PersistentDataType.STRING);
        if ("arquero".equals(rangedSet) && event.getHitBlock() != null && random.nextDouble() < 0.25) {
            ItemStack item = arrow.getItemStack();
            player.getWorld().dropItemNaturally(arrow.getLocation(), item);
        }
    }
}
