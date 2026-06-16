package com.zenex.clans.utils;

import com.zenex.clans.ZenexClans;
import com.zenex.clans.data.Clan;
import com.zenex.clans.data.ClanPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderHook extends PlaceholderExpansion {
    
    private final ZenexClans plugin;
    
    public PlaceholderHook(ZenexClans plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getIdentifier() {
        return "clan";
    }
    
    @Override
    public String getAuthor() {
        return "ZenexPlugins";
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (offlinePlayer == null) return "";
        
        Player player = offlinePlayer.getPlayer();
        ClanPlayer cp = null;
        Clan clan = null;
        
        if (player != null) {
            cp = plugin.getClanManager().getClanPlayer(player);
            if (cp != null && cp.getClanId() != null) {
                clan = plugin.getClanManager().getClan(cp.getClanId());
            }
        }
        
        String main = params.toLowerCase();
        
        if (main.equals("hasclan")) {
            return cp != null && cp.getClanId() != null ? "Да" : "Нет";
        }
        
        if (clan == null) {
            return "";
        }
        
        switch (main) {
            case "name": return clan.getName();
            case "color": return clan.getColor();
            case "members": return String.valueOf(clan.getMemberCount());
            case "maxmembers": return String.valueOf(plugin.getConfig().getInt("clan.max-members", 20));
            case "leader": return clan.getLeaderName();
            case "coleader": return clan.getColeaderName() != null ? clan.getColeaderName() : "Нет";
            case "created": return clan.getCreatedDate();
            case "online": return String.valueOf(clan.getOnlineCount());
            case "role":
                if (cp == null) return "Участник";
                return cp.getRoleName();
            case "rolecolor":
                if (cp == null) return "&7";
                return cp.getRoleColor();
            case "isleader":
                if (cp == null) return "Нет";
                return cp.isLeader() ? "Да" : "Нет";
            case "iscoleader":
                if (cp == null) return "Нет";
                return cp.isColeader() ? "Да" : "Нет";
            default: return "";
        }
    }
}
