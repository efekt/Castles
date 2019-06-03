package it.efekt.mc.castles;

import it.efekt.mc.castles.commands.CastleCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CastlesPlugin extends JavaPlugin {
    public static CastlesManager castlesManager;
    public static JavaPlugin plugin;

    @Override
    public void onEnable(){
        plugin = this;
        registerCommands();
        castlesManager = new CastlesManager(this);
        saveDefaultConfig();
    }

    private void registerCommands(){
        getCommand("castles").setExecutor(new CastleCommand());
    }
}
