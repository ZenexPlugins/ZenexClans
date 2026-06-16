package com.zenex.clans.command;

import com.zenex.clans.ZenexClans;
import com.zenex.clans.data.Clan;
import com.zenex.clans.data.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanCommand implements CommandExecutor {
    
    private final ZenexClans plugin;
    
    public ClanCommand(ZenexClans plugin) {
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
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("clan-create-usage"));
                    return true;
                }
                String name = args[1];
                boolean success = plugin.getClanManager().createClan(name, player);
                if (success) {
                    player.sendMessage(plugin.getMessageManager().getMessage("clan-create-success")
                        .replace("{name}", name));
                } else {
                    player.sendMessage(plugin.getMessageManager().getMessage("clan-create-exists"));
                }
                break;
                
            case "disband":
                Clan disbandClan = plugin.getClanManager().getPlayerClan(player);
                if (disbandClan == null) {
                    player.sendMessage(plugin.getMessageManager().getMessage("not-in-clan"));
                    return true;
                }
                if (!disbandClan.getLeaderUuid().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getMessageManager().getMessage("only-leader"));
                    return true;
                }
                plugin.getClanManager().disbandClan(disbandClan);
                player.sendMessage(plugin.getMessageManager().getMessage("clan-disband")
                    .replace("{name}", disbandClan.getName()));
                break;
                
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("clan-invite-usage"));
                    return true;
                }
                handleInvite(player, args[1]);
                break;
                
            case "accept":
                if (args.length < 2) {
                    player.sendMessage("§c/clan accept <клан>");
                    return true;
                }
                handleAccept(player, args[1]);
                break;
                
            case "deny":
                if (args.length < 2) {
                    player.sendMessage("§c/clan deny <клан>");
                    return true;
                }
                handleDeny(player, args[1]);
                break;
                
            case "list":
                handleList(player);
                break;
                
            case "info":
                handleInfo(player);
                break;
                
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("clan-kick-usage"));
                    return true;
                }
                handleKick(player, args[1]);
                break;
                
            case "addbw":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("clan-addbw-usage"));
                    return true;
                }
                handleAddBlacklist(player, args[1]);
                break;
                
            case "removebw":
                if (args.length < 2) {
                    player.sendMessage(plugin.getMessageManager().getMessage("clan-removebw-usage"));
                    return true;
                }
                handleRemoveBlacklist(player, args[1]);
                break;
                
            case "help":
            default:
                sendHelp(player);
        }
        return true;
    }
    
    private void handleInvite(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("player-not-found"));
            return;
        }
        if (target.equals(player)) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-invite-self"));
            return;
        }
        
        Clan clan = plugin.getClanManager().getPlayerClan(player);
        if (clan == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("not-in-clan"));
            return;
        }
        
        // Проверяем, лидер или солидер
        boolean isLeader = clan.getLeaderUuid().equals(player.getUniqueId());
        boolean isColeader = clan.getColeaderUuid() != null && clan.getColeaderUuid().equals(player.getUniqueId());
        if (!isLeader && !isColeader) {
            player.sendMessage(plugin.getMessageManager().getMessage("only-leader-or-coleader"));
            return;
        }
        
        if (plugin.getClanManager().isInClan(target)) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-invite-already"));
            return;
        }
        if (plugin.getBlacklistManager().isBlacklisted(clan.getId(), target.getName())) {
            player.sendMessage("§c❌ Этот игрок в чёрном списке клана!");
            return;
        }
        
        plugin.getInviteManager().sendInvite(player, target, clan.getName());
        player.sendMessage(plugin.getMessageManager().getMessage("clan-invite-sent")
            .replace("{player}", target.getName()));
        target.sendMessage(plugin.getMessageManager().getMessage("clan-invite-received")
            .replace("{player}", player.getName())
            .replace("{clan}", clan.getName()));
    }
    
    private void handleAccept(Player player, String clanName) {
        if (plugin.getClanManager().isInClan(player)) {
            player.sendMessage(plugin.getMessageManager().getMessage("already-in-clan"));
            return;
        }
        if (!plugin.getInviteManager().hasInvite(player, clanName)) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-accept-not-invited"));
            return;
        }
        
        Clan clan = plugin.getClanManager().getClan(clanName);
        if (clan == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-not-found"));
            return;
        }
        if (clan.getMemberCount() >= plugin.getConfig().getInt("clan.max-members", 20)) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-accept-full")
                .replace("{max}", String.valueOf(plugin.getConfig().getInt("clan.max-members", 20))));
            return;
        }
        
        plugin.getClanManager().addMember(clan, player);
        plugin.getInviteManager().removeInvite(player, clanName);
        player.sendMessage(plugin.getMessageManager().getMessage("clan-accept-success")
            .replace("{clan}", clan.getName()));
    }
    
    private void handleDeny(Player player, String clanName) {
        if (!plugin.getInviteManager().hasInvite(player, clanName)) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-accept-not-invited"));
            return;
        }
        plugin.getInviteManager().removeInvite(player, clanName);
        player.sendMessage(plugin.getMessageManager().getMessage("clan-deny-success")
            .replace("{clan}", clanName));
    }
    
    private void handleList(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player);
        if (clan == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("not-in-clan"));
            return;
        }
        
        player.sendMessage(plugin.getMessageManager().getMessage("clan-list-header")
            .replace("{clan}", clan.getName()));
        
        for (UUID memberUuid : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberUuid);
            String name = member != null ? member.getName() : "Неизвестно";
            
            ClanPlayer cp = plugin.getClanManager().getClanPlayer(memberUuid);
            String roleColor = cp != null ? cp.getRoleColor() : "&7";
            String role = cp != null ? cp.getRoleName() : "Участник";
            
            player.sendMessage(plugin.getMessageManager().getMessage("clan-list-entry")
                .replace("{color}", roleColor)
                .replace("{role}", role)
                .replace("{player}", name));
        }
        if (clan.getMembers().isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-list-empty"));
        }
    }
    
    private void handleInfo(Player player) {
        Clan clan = plugin.getClanManager().getPlayerClan(player);
        if (clan == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("not-in-clan"));
            return;
        }
        
        String coleader = clan.getColeaderName() != null ? clan.getColeaderName() : "Нет";
        
        String info = plugin.getMessageManager().getMessage("clan-info")
            .replace("{name}", clan.getName())
            .replace("{color}", "&b")
            .replace("{leader}", clan.getLeaderName())
            .replace("{coleader}", coleader)
            .replace("{members}", String.valueOf(clan.getMemberCount()))
            .replace("{max}", String.valueOf(plugin.getConfig().getInt("clan.max-members", 20)))
            .replace("{created}", clan.getCreatedDate());
        
        for (String line : info.split("\n")) {
            player.sendMessage(line);
        }
    }
    
    private void handleKick(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("player-not-found"));
            return;
        }
        
        Clan clan = plugin.getClanManager().getPlayerClan(player);
        if (clan == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("not-in-clan"));
            return;
        }
        if (!clan.getLeaderUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("only-leader"));
            return;
        }
        if (clan.getLeaderUuid().equals(target.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-kick-leader"));
            return;
        }
        if (clan.getColeaderUuid() != null && clan.getColeaderUuid().equals(target.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-kick-coleader"));
            return;
        }
        if (!plugin.getClanManager().isInClan(target)) {
            player.sendMessage(plugin.getMessageManager().getMessage("player-not-in-clan"));
            return;
        }
        
        plugin.getClanManager().removeMember(clan, target);
        player.sendMessage(plugin.getMessageManager().getMessage("clan-kick-success")
            .replace("{player}", target.getName()));
        target.sendMessage("§c❌ Вас кикнули из клана " + clan.getName() + "!");
    }
    
    private void handleAddBlacklist(Player player, String targetName) {
        Clan clan = plugin.getClanManager().getPlayerClan(player);
        if (clan == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("not-in-clan"));
            return;
        }
        if (!clan.getLeaderUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("only-leader"));
            return;
        }
        
        if (plugin.getBlacklistManager().isBlacklisted(clan.getId(), targetName)) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-addbw-already"));
            return;
        }
        
        plugin.getBlacklistManager().addToBlacklist(clan.getId(), targetName);
        player.sendMessage(plugin.getMessageManager().getMessage("clan-addbw-success")
            .replace("{player}", targetName));
    }
    
    private void handleRemoveBlacklist(Player player, String targetName) {
        Clan clan = plugin.getClanManager().getPlayerClan(player);
        if (clan == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("not-in-clan"));
            return;
        }
        if (!clan.getLeaderUuid().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("only-leader"));
            return;
        }
        
        if (!plugin.getBlacklistManager().isBlacklisted(clan.getId(), targetName)) {
            player.sendMessage(plugin.getMessageManager().getMessage("clan-removebw-not-found"));
            return;
        }
        
        plugin.getBlacklistManager().removeFromBlacklist(clan.getId(), targetName);
        player.sendMessage(plugin.getMessageManager().getMessage("clan-removebw-success")
            .replace("{player}", targetName));
    }
    
    private void sendHelp(Player player) {
        String help = plugin.getMessageManager().getMessage("clan-help");
        for (String line : help.split("\n")) {
            player.sendMessage(line);
        }
    }
}
