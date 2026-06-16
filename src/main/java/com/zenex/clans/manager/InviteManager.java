package com.zenex.clans.manager;

import com.zenex.clans.ZenexClans;
import org.bukkit.entity.Player;

import java.util.*;

public class InviteManager {
    
    private final ZenexClans plugin;
    private final Map<String, List<Invite>> invites = new HashMap<>();
    
    public InviteManager(ZenexClans plugin) {
        this.plugin = plugin;
    }
    
    public void sendInvite(Player inviter, Player invited, String clanName) {
        String key = invited.getName().toLowerCase();
        invites.computeIfAbsent(key, k -> new ArrayList<>())
               .add(new Invite(inviter.getName(), clanName, System.currentTimeMillis()));
    }
    
    public boolean hasInvite(Player player, String clanName) {
        String key = player.getName().toLowerCase();
        List<Invite> list = invites.get(key);
        if (list == null) return false;
        
        for (Invite inv : list) {
            if (inv.clanName.equalsIgnoreCase(clanName) && !inv.isExpired()) {
                return true;
            }
        }
        return false;
    }
    
    public void removeInvite(Player player, String clanName) {
        String key = player.getName().toLowerCase();
        List<Invite> list = invites.get(key);
        if (list == null) return;
        
        list.removeIf(inv -> inv.clanName.equalsIgnoreCase(clanName));
        if (list.isEmpty()) {
            invites.remove(key);
        }
    }
    
    public void clearExpired() {
        long now = System.currentTimeMillis();
        invites.values().forEach(list -> list.removeIf(inv -> now > inv.expiresAt));
        invites.values().removeIf(List::isEmpty);
    }
    
    private static class Invite {
        final String inviter;
        final String clanName;
        final long createdAt;
        final long expiresAt;
        
        Invite(String inviter, String clanName, long createdAt) {
            this.inviter = inviter;
            this.clanName = clanName;
            this.createdAt = createdAt;
            this.expiresAt = createdAt + 60000;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
