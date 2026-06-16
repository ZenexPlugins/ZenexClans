package com.zenex.clans.manager;

import com.zenex.clans.ZenexClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class BlacklistManager {
    
    private final ZenexClans plugin;
    private final Map<UUID, Set<String>> blacklists = new HashMap<>();
    
    public BlacklistManager(ZenexClans plugin) {
        this.plugin = plugin;
        loadBlacklists();
    }
    
    public void addToBlacklist(UUID clanId, String playerName) {
        blacklists.computeIfAbsent(clanId, k -> new HashSet<>()).add(playerName.toLowerCase());
        saveBlacklist(clanId);
    }
    
    public void removeFromBlacklist(UUID clanId, String playerName) {
        Set<String> list = blacklists.get(clanId);
        if (list != null) {
            list.remove(playerName.toLowerCase());
            if (list.isEmpty()) {
                blacklists.remove(clanId);
            }
            saveBlacklist(clanId);
        }
    }
    
    public boolean isBlacklisted(UUID clanId, String playerName) {
        Set<String> list = blacklists.get(clanId);
        return list != null && list.contains(playerName.toLowerCase());
    }
    
    public Set<String> getBlacklist(UUID clanId) {
        return blacklists.getOrDefault(clanId, new HashSet<>());
    }
    
    private void saveBlacklist(UUID clanId) {
        File file = new File(plugin.getDataFolder(), "blacklists/" + clanId.toString() + ".yml");
        file.getParentFile().mkdirs();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        Set<String> list = blacklists.get(clanId);
        if (list != null && !list.isEmpty()) {
            config.set("blacklist", new ArrayList<>(list));
        } else {
            config.set("blacklist", null);
        }
        
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save blacklist: " + e.getMessage());
        }
    }
    
    private void loadBlacklists() {
        File folder = new File(plugin.getDataFolder(), "blacklists");
        if (!folder.exists()) return;
        
        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;
            try {
                UUID clanId = UUID.fromString(file.getName().replace(".yml", ""));
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                List<String> list = config.getStringList("blacklist");
                if (!list.isEmpty()) {
                    blacklists.put(clanId, new HashSet<>(list));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load blacklist: " + e.getMessage());
            }
        }
    }
}
