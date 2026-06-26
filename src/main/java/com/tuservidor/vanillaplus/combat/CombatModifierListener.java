package com.tuservidor.vanillaplus.combat;

import com.tuservidor.vanillaplus.item.ItemKeys;
import com.tuservidor.vanillaplus.set.PlayerSetState;
import com.tuservidor.vanillaplus.set.SetDefinition;
import com.tuservidor.vanillaplus.set.SetManager;
import com.tuservidor.vanillaplus.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CombatModifierListener implements Listener {
    private final SetManager setManager;

    public CombatModifierListener(SetManager setManager) {
        this.setManager = setManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGenericDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        SetDefinition armor = setManager.getDefinition(setManager.getState(victim).armorSetId);
        if (armor == null) return;
        double multiplier = incomingMultiplier(event.getCause(), armor, null);
        event.setDamage(event.getDamage() * multiplier);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        double multiplier = 1.0;

        if (event.getEntity() instanceof Player victim) {
            SetDefinition armor = setManager.getDefinition(setManager.getState(victim).armorSetId);
            if (armor != null) multiplier *= incomingMultiplier(event.getCause(), armor, event.getDamager());
        }

        Player attacker = attackerOf(event.getDamager());
        if (attacker != null) {
            PlayerSetState state = setManager.getState(attacker);
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (ItemUtil.isSword(weapon.getType())) {
                SetDefinition sword = setManager.getDefinition(state.swordSetId);
                if (sword != null) multiplier *= sword.swordDamageMultiplier;
                if ("calista_sangre".equals(state.armorSetId)) multiplier *= 1.08;
            }
            if (weapon.getType() == Material.MACE) {
                if ("metal_ligero".equals(state.armorSetId)) multiplier *= 1.00; // posicionamiento, no dano gratis
                if ("nieve_abajo".equals(state.armorSetId)) multiplier *= 1.08;
                if ("calista_sangre".equals(state.armorSetId)) multiplier *= 1.06;
            }
        }

        if (event.getDamager() instanceof Projectile projectile) {
            Double projectileMultiplier = projectile.getPersistentDataContainer().get(ItemKeys.PROJECTILE_DAMAGE_MULTIPLIER, PersistentDataType.DOUBLE);
            if (projectileMultiplier != null) multiplier *= projectileMultiplier;
        }

        event.setDamage(Math.max(0.0, event.getDamage() * multiplier));
    }

    private double incomingMultiplier(EntityDamageEvent.DamageCause cause, SetDefinition armor, Entity damager) {
        double multiplier = 1.0;
        if (armor.defenseMultiplier > 0) multiplier *= 1.0 / armor.defenseMultiplier;
        if (isExplosion(cause, damager)) multiplier *= armor.explosionDamageMultiplier;
        if (isProjectile(cause, damager)) multiplier *= armor.projectileTakenMultiplier;
        if (isFire(cause)) multiplier *= armor.fireDamageMultiplier;
        if (cause == EntityDamageEvent.DamageCause.FALL) multiplier *= armor.fallDamageMultiplier;
        if (damager instanceof Player p && p.getInventory().getItemInMainHand().getType() == Material.MACE) {
            if ("arquero".equals(armor.id) || "acero_cristalizado".equals(armor.id)) multiplier *= 1.10;
        }
        return multiplier;
    }

    private boolean isExplosion(EntityDamageEvent.DamageCause cause, Entity damager) {
        return cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || damager instanceof TNTPrimed
                || damager instanceof EnderCrystal
                || damager instanceof Creeper;
    }

    private boolean isProjectile(EntityDamageEvent.DamageCause cause, Entity damager) {
        return cause == EntityDamageEvent.DamageCause.PROJECTILE || damager instanceof Projectile;
    }

    private boolean isFire(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.HOT_FLOOR
                || cause == EntityDamageEvent.DamageCause.CAMPFIRE;
    }

    private Player attackerOf(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }
}
