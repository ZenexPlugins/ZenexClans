package com.zenex.clans.manager;

import com.zenex.clans.ZenexClans;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankManager {
    
    private final ZenexClans plugin;
    private final Map<UUID, Map<Integer, ItemStack>> banks = new HashMap<>();
    
    public BankManager(ZenexClans plugin) {
        this.plugin = plugin;
        loadBanks();
    }
    
    public void saveBank(UUID clanId, Inventory inventory) {
        Map<Integer, ItemStack> items = new HashMap<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != org.bukkit.Material.AIR) {
                items.put(i, item.clone());
            }
        }
        banks.put(clanId, items);
        saveToFile(clanId, items);
    }
    
    public Map<Integer, ItemStack> getBankItems(UUID clanId) {
        return banks.getOrDefault(clanId, new HashMap<>());
    }
    
    private void saveToFile(UUID clanId, Map<Integer, ItemStack> items) {
        File file = new File(plugin.getDataFolder(), "banks/" + clanId.toString() + ".yml");
        file.getParentFile().mkdirs();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            config.set("items." + entry.getKey(), entry.getValue());
        }
        
        try {
            config.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save bank: " + e.getMessage());
        }
    }
    
    private void loadBanks() {
        File bankFolder = new File(plugin.getDataFolder(), "banks");
        if (!bankFolder.exists()) return;
        
        for (File file : bankFolder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;
            try {
                UUID clanId = UUID.fromString(file.getName().replace(".yml", ""));
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                Map<Integer, ItemStack> items = new HashMap<>();
                
                if (config.contains("items")) {
                    for (String key : config.getConfigurationSection("items").getKeys(false)) {
                        int slot = Integer.parseInt(key);
                        ItemStack item = config.getItemStack("items." + key);
                        if (item != null) {
                            items.put(slot, item);
                        }
                    }
                }
                banks.put(clanId, items);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load bank: " + e.getMessage());
            }
        }
    }
}
