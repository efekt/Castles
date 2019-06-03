package it.efekt.mc.castles;

import it.efekt.mc.castles.commands.CastleCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CastlesPlugin extends JavaPlugin {
    public static CastlesManager castlesManager;


    @Override
    public void onEnable(){
        registerCommands();
        castlesManager = new CastlesManager(this);
    }

    private void registerCommands(){
        getCommand("castles").setExecutor(new CastleCommand());
    }
}
