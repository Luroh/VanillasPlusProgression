package com.tuservidor.vanillaplus.item;

import org.bukkit.Material;

public class CustomItemSpec {
    private final String id;
    private final String name;
    private final String category;
    private final String setId;
    private final int customModelData;
    private final String type;
    private final Material material;
    private final String tier;

    public CustomItemSpec(String id, String name, String category, String setId, int customModelData, String type, Material material, String tier) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.setId = setId;
        this.customModelData = customModelData;
        this.type = type;
        this.material = material;
        this.tier = tier;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String category() { return category; }
    public String setId() { return setId; }
    public int customModelData() { return customModelData; }
    public String type() { return type; }
    public Material material() { return material; }
    public String tier() { return tier; }
}
