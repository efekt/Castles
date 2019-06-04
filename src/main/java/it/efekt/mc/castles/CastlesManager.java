package it.efekt.mc.castles;

import org.bukkit.event.HandlerList;

public class CastlesManager {
    private Castles instance;
    private CastlesPlugin plugin;

    public CastlesManager(CastlesPlugin castles){
        this.plugin = castles;
        init();
    }

    private void init(){
        // Unregister previously registered listener
        if (this.instance != null){
            HandlerList.unregisterAll(instance);
        }

        this.instance = new Castles(this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this.instance, this.plugin);

    }

    public void start(){
        this.instance.start();
    }

    public void progress(){
        this.instance.progress();
    }

    public Castles getInstance(){
        return this.instance;
    }
}
