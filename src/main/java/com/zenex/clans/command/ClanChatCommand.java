package com.zenex.clans.command;

import com.zenex.clans.ZenexClans;
import com.zenex.clans.data.Clan;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanChatCommand implements CommandExecutor {
    
    private final ZenexClans plugin;
    
    public ClanChatCommand(ZenexClans plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cТолько для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage("§c/clanchat <сообщение>");
            return true;
        }
        
        Clan clan = plugin.getClanManager().getPlayerClan(player);
        if (clan == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("not-in-clan"));
            return true;
        }
        
        String message = String.join(" ", args);
        String format = "§7[§b" + clan.getName() + "§7] §f" + player.getName() + "§7: §f" + message;
        
        for (Player member : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getClanManager().isInClan(member) && 
                plugin.getClanManager().getPlayerClan(member).getId().equals(clan.getId())) {
                member.sendMessage(format);
            }
        }
        return true;
    }
}
