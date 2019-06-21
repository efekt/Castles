package it.efekt.mc.castles.commands;

import it.efekt.mc.castles.*;
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
        if (args.length == 1){
            switch (args[0]){
                case "start":
                    CastlesPlugin.castlesManager.start();
                    Castles castles = CastlesPlugin.castlesManager.getInstance();
                    Config config = castles.getConfig();
                    Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Zamki rozpoczęte!");
                    Bukkit.broadcastMessage(" ");
                    Bukkit.broadcastMessage(ChatColor.AQUA + "Czas pokoju: " + ChatColor.GOLD + config.getPeaceTime());
                    Bukkit.broadcastMessage(ChatColor.AQUA + "Czas wojny: " + ChatColor.GOLD + config.getWarTime());
                    Bukkit.broadcastMessage(ChatColor.AQUA + "Drużyny: " + ChatColor.GOLD + config.getTeamCount());
                    int i = 1;
                    for (CastleTeam team : castles.getTeams()){
                        Bukkit.broadcastMessage(team.getColor() + "" + ChatColor.BOLD + castles.getConfig().getTeamBaseName() + " " + i + ": " + team.getColor() + team.getPlayersAsString());
                        i++;
                    }
                    return true;
                case "next":
                    if (CastlesPlugin.castlesManager.getInstance() == null || CastlesPlugin.castlesManager.getInstance().getGameState().equals(GameState.LOBBY)){
                        sender.sendMessage("You need to start castles first!");
                        return true;
                    }
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
