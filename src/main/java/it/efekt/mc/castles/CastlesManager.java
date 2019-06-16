package it.efekt.mc.castles;

import org.bukkit.event.HandlerList;

public class CastlesManager {
    private Castles instance;
    private CastlesPlugin plugin;

    public CastlesManager(CastlesPlugin castles){
        this.plugin = castles;
    }

    private void init(){
        // Unregister previously registered listener
        if (this.instance != null){
            HandlerList.unregisterAll(plugin);
        }

        this.instance = new Castles(this.plugin);

    }

    public void start(){
        this.init();
        this.instance.start();
    }

    public void progress(){
        this.instance.next();
    }

    public Castles getInstance(){
        return this.instance;
    }
}
