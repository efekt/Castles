package it.efekt.mc.castles.commands;

import it.efekt.mc.castles.CastlesPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CastleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1){

            if (args[0].equalsIgnoreCase("start")){
                CastlesPlugin.castlesManager.start();
                return true;
            }
            // force next gamestate
            if (args[0].equalsIgnoreCase("next")){
                CastlesPlugin.castlesManager.progress();
            }

        }
        return true;
    }
}
