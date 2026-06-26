package com.tuservidor.vanillaplus.plant;

import org.bukkit.Material;

public class PlantDefinition {
    public final String seedId;
    public final String productId;
    public final String name;
    public final Material visual;
    public final Material[] validBases;
    public final int maxStage;
    public final boolean legendary;

    public PlantDefinition(String seedId, String productId, String name, Material visual, int maxStage, boolean legendary, Material... validBases) {
        this.seedId = seedId;
        this.productId = productId;
        this.name = name;
        this.visual = visual;
        this.validBases = validBases;
        this.maxStage = maxStage;
        this.legendary = legendary;
    }

    public boolean isValidBase(Material material) {
        for (Material valid : validBases) if (valid == material) return true;
        return false;
    }
}
