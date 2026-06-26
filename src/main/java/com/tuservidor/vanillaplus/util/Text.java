package com.tuservidor.vanillaplus.util;

import org.bukkit.ChatColor;

public final class Text {
    private Text() {}

    public static String color(String input) {
        if (input == null) return "";
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public static String stripNamespace(String id) {
        if (id == null) return null;
        return id.startsWith("pp:") ? id.substring(3) : id;
    }
}
