package com.zenex.clans.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.text.SimpleDateFormat;
import java.util.*;

public class Clan {
    
    private final UUID id;
    private final String name;
    private String color;
    private final UUID leaderUuid;
    private String leaderName;
    private UUID coleaderUuid;
    private String coleaderName;
    private final Set<UUID> members = new HashSet<>();
    private final long createdAt;
    
    public Clan(UUID id, String name, String color, UUID leaderUuid, String leaderName) {
        this.id = id;
        this.name = name;
        this.color = color != null ? color : "&b";
        this.leaderUuid = leaderUuid;
        this.leaderName = leaderName;
        this.coleaderUuid = null;
        this.coleaderName = null;
        this.createdAt = System.currentTimeMillis();
        this.members.add(leaderUuid);
    }
    
    public Clan(UUID id, String name, String color, UUID leaderUuid, String leaderName, 
                UUID coleaderUuid, String coleaderName, long createdAt) {
        this.id = id;
        this.name = name;
        this.color = color != null ? color : "&b";
        this.leaderUuid = leaderUuid;
        this.leaderName = leaderName;
        this.coleaderUuid = coleaderUuid;
        this.coleaderName = coleaderName;
        this.createdAt = createdAt;
        this.members.add(leaderUuid);
        if (coleaderUuid != null) {
            this.members.add(coleaderUuid);
        }
    }
    
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public UUID getLeaderUuid() { return leaderUuid; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String name) { this.leaderName = name; }
    public UUID getColeaderUuid() { return coleaderUuid; }
    public void setColeaderUuid(UUID uuid) { this.coleaderUuid = uuid; }
    public String getColeaderName() { return coleaderName; }
    public void setColeaderName(String name) { this.coleaderName = name; }
    public Set<UUID> getMembers() { return members; }
    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }
    public int getMemberCount() { return members.size(); }
    public long getCreatedAt() { return createdAt; }
    public String getCreatedDate() { return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(createdAt)); }
    public int getOnlineCount() {
        int count = 0;
        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) count++;
        }
        return count;
    }
    
    public boolean isLeader(UUID uuid) {
        return leaderUuid.equals(uuid);
    }
    
    public boolean isColeader(UUID uuid) {
        return coleaderUuid != null && coleaderUuid.equals(uuid);
    }
    
    public boolean isStaff(UUID uuid) {
        return isLeader(uuid) || isColeader(uuid);
    }
    
    public String getFormattedName() {
        return color + name;
    }
}
