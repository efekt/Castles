package it.efekt.mc.castles.commands;

import it.efekt.mc.castles.CastleTeam;
import it.efekt.mc.castles.Castles;
import it.efekt.mc.castles.CastlesPlugin;
import it.efekt.mc.castles.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CastleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)){
            sender.sendMessage("This command can by only used as a Player");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 1){

            switch (args[0]){
                case "start":
                    CastlesPlugin.castlesManager.start();
                    Castles castles = CastlesPlugin.castlesManager.getInstance();
                    Config config = castles.getConfig();
                    Bukkit.broadcastMessage(ChatColor.AQUA + "Castles started!");
                    Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Peace time: " + ChatColor.GOLD + config.getPeaceTime());
                    Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "War time: " + ChatColor.GOLD + config.getWarTime());
                    Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Teams: " + ChatColor.GOLD + config.getTeamCount());
                    int i = 1;
                    for (CastleTeam team : castles.getTeams()){
                        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "Team " + i + ": " + ChatColor.WHITE + team.getPlayersAsString());
                        i++;
                    }
                    return true;
                case "next":
                    CastlesPlugin.castlesManager.progress();
                    return true;
                case "reload":
                    CastlesPlugin.plugin.reloadConfig();
                    sender.sendMessage("Config reloaded.");
                    return true;
                default:
                    return false;

            }

        }
        return true;
    }
}
