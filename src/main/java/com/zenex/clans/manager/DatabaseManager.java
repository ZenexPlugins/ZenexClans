package com.zenex.clans.manager;

import com.zenex.clans.ZenexClans;
import com.zenex.clans.data.Clan;
import com.zenex.clans.data.ClanPlayer;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    
    private final ZenexClans plugin;
    private Connection connection;
    
    public DatabaseManager(ZenexClans plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + 
                         "/" + plugin.getConfig().getString("database.sqlite.file", "ZenexClans.db");
            connection = DriverManager.getConnection(url);
            createTables();
            plugin.getLogger().info("✅ Database connected!");
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Database error: " + e.getMessage());
        }
    }
    
    private void createTables() throws SQLException {
        String clans = """
            CREATE TABLE IF NOT EXISTS clans (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL UNIQUE,
                color TEXT DEFAULT '&b',
                leader_uuid TEXT NOT NULL,
                leader_name TEXT NOT NULL,
                coleader_uuid TEXT,
                coleader_name TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        String players = """
            CREATE TABLE IF NOT EXISTS players (
                uuid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                clan_id TEXT,
                role TEXT DEFAULT 'MEMBER',
                FOREIGN KEY (clan_id) REFERENCES clans(id)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(clans);
            stmt.execute(players);
        }
    }
    
    public void saveClan(Clan clan) {
        String sql = "INSERT OR REPLACE INTO clans (id, name, color, leader_uuid, leader_name, coleader_uuid, coleader_name) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, clan.getId().toString());
            stmt.setString(2, clan.getName());
            stmt.setString(3, clan.getColor());
            stmt.setString(4, clan.getLeaderUuid().toString());
            stmt.setString(5, clan.getLeaderName());
            stmt.setString(6, clan.getColeaderUuid() != null ? clan.getColeaderUuid().toString() : null);
            stmt.setString(7, clan.getColeaderName());
            stmt.executeUpdate();
        } catch (SQLException e) {}
    }
    
    public void deleteClan(UUID id) {
        String sql = "DELETE FROM clans WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {}
    }
    
    public Clan getClan(String name) {
        String sql = "SELECT * FROM clans WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String coleaderUuidStr = rs.getString("coleader_uuid");
                UUID coleaderUuid = coleaderUuidStr != null ? UUID.fromString(coleaderUuidStr) : null;
                return new Clan(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("name"),
                    rs.getString("color"),
                    UUID.fromString(rs.getString("leader_uuid")),
                    rs.getString("leader_name"),
                    coleaderUuid,
                    rs.getString("coleader_name"),
                    rs.getTimestamp("created_at").getTime()
                );
            }
        } catch (SQLException e) {}
        return null;
    }
    
    public List<Clan> getAllClans() {
        List<Clan> clans = new ArrayList<>();
        String sql = "SELECT * FROM clans";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String coleaderUuidStr = rs.getString("coleader_uuid");
                UUID coleaderUuid = coleaderUuidStr != null ? UUID.fromString(coleaderUuidStr) : null;
                clans.add(new Clan(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("name"),
                    rs.getString("color"),
                    UUID.fromString(rs.getString("leader_uuid")),
                    rs.getString("leader_name"),
                    coleaderUuid,
                    rs.getString("coleader_name"),
                    rs.getTimestamp("created_at").getTime()
                ));
            }
        } catch (SQLException e) {}
        return clans;
    }
    
    public void savePlayer(ClanPlayer player) {
        String sql = "INSERT OR REPLACE INTO players (uuid, name, clan_id, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player.getUuid().toString());
            stmt.setString(2, player.getName());
            stmt.setString(3, player.getClanId() != null ? player.getClanId().toString() : null);
            stmt.setString(4, player.getRole().name());
            stmt.executeUpdate();
        } catch (SQLException e) {}
    }
    
    public ClanPlayer getPlayer(UUID uuid) {
        String sql = "SELECT * FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String clanIdStr = rs.getString("clan_id");
                UUID clanId = clanIdStr != null ? UUID.fromString(clanIdStr) : null;
                return new ClanPlayer(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getString("name"),
                    clanId,
                    ClanPlayer.Role.valueOf(rs.getString("role"))
                );
            }
        } catch (SQLException e) {}
        return null;
    }
    
    public void deletePlayer(UUID uuid) {
        String sql = "DELETE FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {}
    }
    
    public void deleteAllPlayers(UUID clanId) {
        String sql = "DELETE FROM players WHERE clan_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, clanId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {}
    }
    
    public Connection getConnection() { return connection; }
    public void close() { try { if (connection != null && !connection.isClosed()) connection.close(); } catch (SQLException e) {} }
}
