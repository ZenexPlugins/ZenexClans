package com.zenex.clans.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    
    private final JavaPlugin plugin;
    private final Map<String, String> messages = new HashMap<>();
    
    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    public void loadMessages() {
        File f = new File(plugin.getDataFolder(), "messages.yml");
        if (!f.exists()) plugin.saveResource("messages.yml", false);
        FileConfiguration config = YamlConfiguration.loadConfiguration(f);
        messages.clear();
        if (config.contains("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                messages.put(key, config.getString("messages." + key));
            }
        }
    }
    
    public String getMessage(String key) {
        String msg = messages.get(key);
        return msg != null ? ChatColor.translateAlternateColorCodes('&', msg) : "§cСообщение не найдено: " + key;
    }
    
    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }
        return msg;
    }
    
    public void reload() { loadMessages(); }
}
