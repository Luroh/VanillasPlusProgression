package com.tuservidor.vanillaplus.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration setsConfig;
    private FileConfiguration messagesConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        writeIfMissing("config.yml", defaultConfig());
        writeIfMissing("sets.yml", defaultSets());
        writeIfMissing("messages.yml", defaultMessages());
        mainConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        setsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "sets.yml"));
        messagesConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    private void writeIfMissing(String name, String content) {
        File file = new File(plugin.getDataFolder(), name);
        if (file.exists()) return;
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo escribir " + name, e);
        }
    }

    public FileConfiguration main() { return mainConfig; }
    public FileConfiguration sets() { return setsConfig; }
    public FileConfiguration messages() { return messagesConfig; }

    private String defaultConfig() {
        return "teleport:\n" +
                "  warmup_seconds: 5\n" +
                "  cancel_on_move_block: true\n" +
                "  cancel_on_damage: true\n" +
                "  tpa_request_seconds: 60\n" +
                "  tpa_cooldown_seconds: 60\n" +
                "homes:\n" +
                "  default_limit: 1\n" +
                "  home_crystal_increment: 1\n" +
                "  max_limit: 10\n" +
                "amulets:\n" +
                "  require_offhand: true\n" +
                "performance:\n" +
                "  allow_locked_drops: false\n" +
                "  save_interval_minutes: 5\n" +
                "  debug: false\n" +
                "fishing:\n" +
                "  legendary_cooldown_seconds: 45\n";
    }

    private String defaultMessages() {
        return "messages:\n" +
                "  unlock_success: '&aCategoria desbloqueada: &f{category}'\n" +
                "  unlock_already: '&eEsa categoria ya estaba desbloqueada.'\n" +
                "  unlock_dependency: '&cPrimero debes desbloquear: {dependency}'\n" +
                "  recipe_locked: '&cEsta receta aun no esta desbloqueada.'\n" +
                "  utils_locked: '&cLas utilidades aun no estan desbloqueadas.'\n" +
                "  home_set: '&aHome guardado: &f{home}'\n" +
                "  home_deleted: '&aHome eliminado: &f{home}'\n" +
                "  home_missing: '&cEse home no existe.'\n" +
                "  home_limit: '&cNo puedes tener mas homes. Usa un Home Crystal para aumentar el limite.'\n" +
                "  home_crystal_used: '&aTu limite de homes aumento a &f{limit}&a.'\n" +
                "  tpa_sent: '&aSolicitud enviada a &f{target}&a.'\n" +
                "  tpa_received: '&f{player} &equiere teletransportarse a ti. Usa /tpaccept o /tpdeny.'\n" +
                "  tpa_none: '&cNo tienes solicitudes pendientes.'\n" +
                "  tpa_denied: '&eSolicitud rechazada.'\n" +
                "  teleport_cancelled_move: '&cTeletransporte cancelado por movimiento.'\n" +
                "  teleport_cancelled_damage: '&cTeletransporte cancelado por dano.'\n" +
                "  teleport_done: '&aTeletransporte completado.'\n";
    }

    private String defaultSets() {
        return "# Todo el balance pasivo se puede editar aqui.\n" +
                "# early_equivalence: armadura/espada base diamante. mid_equivalence: netherite. late_equivalence: netherite + modificadores custom.\n" +
                "sets:\n" +
                set("pluma", "early", 0.10, 0.88, 1.12, 1.0, 1.0, 0.70, 1.0, 1.06, 1.0, 1.0, 1.0, 1.0, 1.0) +
                set("musgo", "early", -0.05, 1.0, 1.10, 1.0, 1.15, 1.0, 1.0, 1.0, 1.0, 1.0, 1.08, 0.92, 0.80) +
                set("nieve", "early", 0.0, 1.0, 1.0, 0.90, 1.20, 1.0, 1.0, 1.0, 1.05, 1.0, 1.0, 1.0, 1.0) +
                set("miel", "early", -0.08, 1.0, 1.0, 0.92, 1.08, 1.0, 1.0, 1.0, 1.0, 1.05, 1.0, 1.0, 0.85) +
                set("tiempo_rojo", "mid", -0.02, 1.0, 1.08, 1.0, 1.0, 1.0, 0.94, 1.0, 1.0, 1.0, 1.12, 0.92, 1.0) +
                set("arquero", "mid", 0.0, 0.90, 1.0, 0.90, 1.0, 1.0, 0.92, 1.10, 1.0, 1.0, 1.0, 1.0, 1.0) +
                set("metal_ligero", "mid", 0.12, 1.0, 1.0, 1.0, 1.10, 1.0, 1.06, 1.0, 1.0, 0.92, 1.0, 1.0, 0.80) +
                set("acero_cristalizado", "mid", -0.07, 1.0, 0.92, 0.86, 1.0, 1.0, 0.95, 1.0, 1.08, 1.0, 1.0, 1.0, 1.0) +
                set("diamante_oscuro", "late", -0.12, 1.12, 0.82, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.88, 1.0, 1.0, 1.15) +
                set("oceano_electrico", "late", 0.0, 0.92, 1.0, 1.0, 1.12, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.90, 1.0) +
                set("calista_sangre", "late", 0.0, 0.90, 1.10, 1.12, 1.0, 1.0, 1.08, 1.0, 1.0, 1.10, 1.08, 0.92, 1.0) +
                set("nieve_abajo", "late", 0.04, 1.0, 0.90, 1.10, 0.75, 1.0, 1.02, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
    }

    private String set(String id, String tier, double movement, double defense, double explosion, double projectileTaken,
                       double fire, double fall, double sword, double bowVelocity, double crossbowVelocity,
                       double instantHeal, double positiveDuration, double negativeDuration, double hunger) {
        return "  " + id + ":\n" +
                "    tier: " + tier + "\n" +
                "    movement_speed: " + movement + "\n" +
                "    defense_multiplier: " + defense + "\n" +
                "    explosion_damage_multiplier: " + explosion + "\n" +
                "    projectile_taken_multiplier: " + projectileTaken + "\n" +
                "    fire_damage_multiplier: " + fire + "\n" +
                "    fall_damage_multiplier: " + fall + "\n" +
                "    sword_damage_multiplier: " + sword + "\n" +
                "    bow_projectile_velocity: " + bowVelocity + "\n" +
                "    crossbow_projectile_velocity: " + crossbowVelocity + "\n" +
                "    instant_potion_heal_multiplier: " + instantHeal + "\n" +
                "    positive_potion_duration_multiplier: " + positiveDuration + "\n" +
                "    negative_potion_duration_multiplier: " + negativeDuration + "\n" +
                "    hunger_multiplier: " + hunger + "\n";
    }
}
