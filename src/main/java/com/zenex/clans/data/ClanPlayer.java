package com.zenex.clans.data;
import java.util.UUID;

public class ClanPlayer {
    
    public enum Role {
        LEADER,
        COLEADER,
        MEMBER
    }
    
    private final UUID uuid;
    private final String name;
    private UUID clanId;
    private Role role;
    
    public ClanPlayer(UUID uuid, String name) {
        this(uuid, name, null, Role.MEMBER);
    }
    
    public ClanPlayer(UUID uuid, String name, UUID clanId, Role role) {
        this.uuid = uuid;
        this.name = name;
        this.clanId = clanId;
        this.role = role;
    }
    
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public UUID getClanId() { return clanId; }
    public void setClanId(UUID clanId) { this.clanId = clanId; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isLeader() { return role == Role.LEADER; }
    public boolean isColeader() { return role == Role.COLEADER; }
    public boolean isMember() { return role == Role.MEMBER; }
    
    public String getRoleName() {
        switch (role) {
            case LEADER: return "👑 Лидер";
            case COLEADER: return "⭐ Солидер";
            default: return "Участник";
        }
    }
    
    public String getRoleColor() {
        switch (role) {
            case LEADER: return "&c";
            case COLEADER: return "&6";
            default: return "&7";
        }
    }
}
