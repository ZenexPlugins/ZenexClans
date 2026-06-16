package com.zenex.clans.manager;

import com.zenex.clans.ZenexClans;
import com.zenex.clans.data.Clan;
import com.zenex.clans.data.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanManager {
    
    private final ZenexClans plugin;
    private final Map<UUID, Clan> clans = new HashMap<>();
    private final Map<UUID, ClanPlayer> players = new HashMap<>();
    
    public ClanManager(ZenexClans plugin) {
        this.plugin = plugin;
        loadAll();
    }
    
    private void loadAll() {
        clans.clear();
        players.clear();
        
        for (Clan clan : plugin.getDatabaseManager().getAllClans()) {
            clans.put(clan.getId(), clan);
            for (UUID memberUuid : clan.getMembers()) {
                ClanPlayer cp = plugin.getDatabaseManager().getPlayer(memberUuid);
                if (cp != null) {
                    players.put(memberUuid, cp);
                }
            }
        }
        
        plugin.getLogger().info("✅ Загружено " + clans.size() + " кланов");
    }
    
    public boolean createClan(String name, Player leader) {
        if (clanExists(name)) return false;
        if (isInClan(leader)) return false;
        
        UUID id = UUID.randomUUID();
        String color = "&b";
        Clan clan = new Clan(id, name, color, leader.getUniqueId(), leader.getName());
        clans.put(id, clan);
        plugin.getDatabaseManager().saveClan(clan);
        
        ClanPlayer cp = new ClanPlayer(leader.getUniqueId(), leader.getName(), id, ClanPlayer.Role.LEADER);
        players.put(leader.getUniqueId(), cp);
        plugin.getDatabaseManager().savePlayer(cp);
        
        return true;
    }
    
    public boolean disbandClan(Clan clan) {
        if (clan == null) return false;
        
        for (UUID memberUuid : clan.getMembers()) {
            players.remove(memberUuid);
            plugin.getDatabaseManager().deletePlayer(memberUuid);
        }
        
        clans.remove(clan.getId());
        plugin.getDatabaseManager().deleteClan(clan.getId());
        plugin.getDatabaseManager().deleteAllPlayers(clan.getId());
        
        return true;
    }
    
    public Clan getClan(String name) {
        for (Clan clan : clans.values()) {
            if (clan.getName().equalsIgnoreCase(name)) {
                return clan;
            }
        }
        return null;
    }
    
    public Clan getClan(UUID id) {
        return clans.get(id);
    }
    
    public boolean clanExists(String name) {
        return getClan(name) != null;
    }
    
    public Clan getPlayerClan(Player player) {
        ClanPlayer cp = players.get(player.getUniqueId());
        if (cp == null || cp.getClanId() == null) return null;
        return clans.get(cp.getClanId());
    }
    
    public ClanPlayer getClanPlayer(Player player) {
        return players.get(player.getUniqueId());
    }
    
    public ClanPlayer getClanPlayer(UUID uuid) {
        return players.get(uuid);
    }
    
    public boolean isInClan(Player player) {
        ClanPlayer cp = players.get(player.getUniqueId());
        return cp != null && cp.getClanId() != null;
    }
    
    public boolean addMember(Clan clan, Player player) {
        if (clan == null || player == null) return false;
        if (isInClan(player)) return false;
        if (clan.getMemberCount() >= plugin.getConfig().getInt("clan.max-members", 20)) return false;
        
        clan.addMember(player.getUniqueId());
        ClanPlayer cp = new ClanPlayer(player.getUniqueId(), player.getName(), clan.getId(), ClanPlayer.Role.MEMBER);
        players.put(player.getUniqueId(), cp);
        plugin.getDatabaseManager().savePlayer(cp);
        
        return true;
    }
    
    public boolean removeMember(Clan clan, Player player) {
        if (clan == null || player == null) return false;
        
        clan.removeMember(player.getUniqueId());
        players.remove(player.getUniqueId());
        plugin.getDatabaseManager().deletePlayer(player.getUniqueId());
        return true;
    }
    
    public boolean setColeader(Clan clan, Player player) {
        if (clan == null || player == null) return false;
        if (!isInClan(player)) return false;
        if (clan.getLeaderUuid().equals(player.getUniqueId())) return false;
        
        clan.setColeaderUuid(player.getUniqueId());
        clan.setColeaderName(player.getName());
        
        ClanPlayer cp = players.get(player.getUniqueId());
        if (cp != null) {
            cp.setRole(ClanPlayer.Role.COLEADER);
            plugin.getDatabaseManager().savePlayer(cp);
        }
        
        plugin.getDatabaseManager().saveClan(clan);
        return true;
    }
    
    public boolean removeColeader(Clan clan) {
        if (clan == null) return false;
        if (clan.getColeaderUuid() == null) return false;
        
        UUID oldColeader = clan.getColeaderUuid();
        clan.setColeaderUuid(null);
        clan.setColeaderName(null);
        
        ClanPlayer cp = players.get(oldColeader);
        if (cp != null) {
            cp.setRole(ClanPlayer.Role.MEMBER);
            plugin.getDatabaseManager().savePlayer(cp);
        }
        
        plugin.getDatabaseManager().saveClan(clan);
        return true;
    }
    
    public List<Clan> getAllClans() {
        return new ArrayList<>(clans.values());
    }
}
