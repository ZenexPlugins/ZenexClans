package com.zenex.clans.command;

import com.zenex.clans.ZenexClans;
import com.zenex.clans.data.Clan;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanAdminCommand implements CommandExecutor {
    
    private final ZenexClans plugin;
    
    public ClanAdminCommand(ZenexClans plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("zenexclans.admin")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage("§c/clanadmin <reload|reset|delete>");
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                plugin.getMessageManager().reload();
                sender.sendMessage("§a✅ Конфигурация перезагружена!");
                break;
                
            case "reset":
                if (args.length < 2) {
                    sender.sendMessage("§c/clanadmin reset <игрок>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("player-not-found"));
                    return true;
                }
                Clan clan = plugin.getClanManager().getPlayerClan(target);
                if (clan == null) {
                    sender.sendMessage("§c❌ Игрок не в клане!");
                    return true;
                }
                plugin.getClanManager().removeMember(clan, target);
                sender.sendMessage("§a✅ Игрок " + target.getName() + " удалён из клана!");
                break;
                
            case "delete":
                if (args.length < 2) {
                    sender.sendMessage("§c/clanadmin delete <клан>");
                    return true;
                }
                Clan deleteClan = plugin.getClanManager().getClan(args[1]);
                if (deleteClan == null) {
                    sender.sendMessage(plugin.getMessageManager().getMessage("clan-not-found"));
                    return true;
                }
                plugin.getClanManager().disbandClan(deleteClan);
                sender.sendMessage("§a✅ Клан " + deleteClan.getName() + " удалён!");
                break;
                
            default:
                sender.sendMessage("§c/clanadmin <reload|reset|delete>");
        }
        return true;
    }
}
