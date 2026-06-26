package com.tuservidor.vanillaplus.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class DataStore {
    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration config;

    public DataStore(JavaPlugin plugin, String relativePath) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), relativePath);
        reload();
    }

    public void reload() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo crear " + file.getName(), e);
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get() {
        return config;
    }

    public void save() {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            File temp = new File(file.getParentFile(), file.getName() + ".tmp");
            config.save(temp);
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception atomicFailed) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "No se pudo guardar " + file.getName(), e);
            }
        }
    }
}
