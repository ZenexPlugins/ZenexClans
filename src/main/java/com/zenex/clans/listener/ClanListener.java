package com.zenex.clans.listener;

import com.zenex.clans.ZenexClans;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ClanListener implements Listener {
    
    private final ZenexClans plugin;
    
    public ClanListener(ZenexClans plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Загружаем данные игрока при входе
        plugin.getClanManager().getClanPlayer(event.getPlayer());
    }
}
