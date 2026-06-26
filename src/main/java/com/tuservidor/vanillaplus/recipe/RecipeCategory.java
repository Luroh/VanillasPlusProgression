package com.tuservidor.vanillaplus.recipe;

import java.util.Locale;

public enum RecipeCategory {
    EARLY,
    MID,
    LATE,
    PLANTAS,
    PESCA,
    UTILS;

    public String id() { return name().toLowerCase(Locale.ROOT); }

    public static RecipeCategory from(String value) {
        if (value == null) return null;
        try { return RecipeCategory.valueOf(value.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException ex) { return null; }
    }
}
