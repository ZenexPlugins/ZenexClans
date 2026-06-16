package com.zenex.clans;

import com.zenex.clans.command.ClanCommand;
import com.zenex.clans.listener.ClanListener;
import com.zenex.clans.manager.*;
import com.zenex.clans.utils.MessageManager;
import com.zenex.clans.utils.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ZenexClans extends JavaPlugin {
    
    private static ZenexClans instance;
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private InviteManager inviteManager;
    private BlacklistManager blacklistManager;
    private MessageManager messageManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        printStartupArt();
        
        saveDefaultConfig();
        saveResource("messages.yml", false);
        
        databaseManager = new DatabaseManager(this);
        databaseManager.initialize();
        
        clanManager = new ClanManager(this);
        inviteManager = new InviteManager(this);
        blacklistManager = new BlacklistManager(this);
        messageManager = new MessageManager(this);
        
        getCommand("clan").setExecutor(new ClanCommand(this));
        
        Bukkit.getPluginManager().registerEvents(new ClanListener(this), this);
        
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
            getLogger().info("✅ PlaceholderAPI hook enabled!");
        }
        
        getLogger().info("✅ ZenexClans v" + getDescription().getVersion() + " enabled!");
        getLogger().info("🏰 Клановая система ZenexClans активна!");
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("ZenexClans disabled!");
    }
    
    private void printStartupArt() {
        String[] art = {
            "§b╔══════════════════════════════════════════════════════════════════╗",
            "§b║                                                                  ║",
            "§b║   §6██████╗  ██████╗                                           §b║",
            "§b║   §6██╔══██╗██╔═══██╗                                          §b║",
            "§b║   §6██████╔╝██║   ██║                                          §b║",
            "§b║   §6██╔═══╝ ██║   ██║                                          §b║",
            "§b║   §6██║     ╚██████╔╝                                          §b║",
            "§b║   §6╚═╝      ╚═════╝                                           §b║",
            "§b║                                                                  ║",
            "§b║   §6§lZP §f§lZenexClans §ev" + getDescription().getVersion() + "          §fBy §eZenexPlugins   §b║",
            "§b║   §7🏰 Клановая система с чёрным списком                       §b║",
            "§b║                                                                  ║",
            "§b╚══════════════════════════════════════════════════════════════════╝"
        };
        
        for (String line : art) {
            getLogger().info(line.replace("§", ""));
        }
    }
    
    public static ZenexClans getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public ClanManager getClanManager() {
        return clanManager;
    }
    
    public InviteManager getInviteManager() {
        return inviteManager;
    }
    
    public BlacklistManager getBlacklistManager() {
        return blacklistManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
}
