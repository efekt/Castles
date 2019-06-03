package it.efekt.mc.castles.commands;

import it.efekt.mc.castles.CastlesPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CastleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1){

            switch (args[0]){
                case "start":
                    CastlesPlugin.castlesManager.start();
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
