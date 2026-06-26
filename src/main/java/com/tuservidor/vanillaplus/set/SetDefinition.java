package com.tuservidor.vanillaplus.set;

import org.bukkit.configuration.ConfigurationSection;

public class SetDefinition {
    public final String id;
    public final String tier;
    public final double movementSpeed;
    public final double defenseMultiplier;
    public final double explosionDamageMultiplier;
    public final double projectileTakenMultiplier;
    public final double fireDamageMultiplier;
    public final double fallDamageMultiplier;
    public final double swordDamageMultiplier;
    public final double bowProjectileVelocity;
    public final double crossbowProjectileVelocity;
    public final double instantPotionHealMultiplier;
    public final double positivePotionDurationMultiplier;
    public final double negativePotionDurationMultiplier;
    public final double hungerMultiplier;

    public SetDefinition(String id, ConfigurationSection section) {
        this.id = id;
        this.tier = section.getString("tier", "early");
        this.movementSpeed = section.getDouble("movement_speed", 0.0);
        this.defenseMultiplier = section.getDouble("defense_multiplier", 1.0);
        this.explosionDamageMultiplier = section.getDouble("explosion_damage_multiplier", 1.0);
        this.projectileTakenMultiplier = section.getDouble("projectile_taken_multiplier", 1.0);
        this.fireDamageMultiplier = section.getDouble("fire_damage_multiplier", 1.0);
        this.fallDamageMultiplier = section.getDouble("fall_damage_multiplier", 1.0);
        this.swordDamageMultiplier = section.getDouble("sword_damage_multiplier", 1.0);
        this.bowProjectileVelocity = section.getDouble("bow_projectile_velocity", 1.0);
        this.crossbowProjectileVelocity = section.getDouble("crossbow_projectile_velocity", 1.0);
        this.instantPotionHealMultiplier = section.getDouble("instant_potion_heal_multiplier", 1.0);
        this.positivePotionDurationMultiplier = section.getDouble("positive_potion_duration_multiplier", 1.0);
        this.negativePotionDurationMultiplier = section.getDouble("negative_potion_duration_multiplier", 1.0);
        this.hungerMultiplier = section.getDouble("hunger_multiplier", 1.0);
    }
}
