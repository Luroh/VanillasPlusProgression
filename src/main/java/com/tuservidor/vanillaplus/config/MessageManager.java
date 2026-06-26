package com.tuservidor.vanillaplus.config;

import com.tuservidor.vanillaplus.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public class MessageManager {
    private final FileConfiguration messages;

    public MessageManager(ConfigManager configManager) {
        this.messages = configManager.messages();
    }

    public String get(String key) {
        return Text.color(messages.getString("messages." + key, key));
    }

    public String format(String key, Map<String, String> placeholders) {
        String msg = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return msg;
    }

    public void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(format(key, placeholders));
    }
}
