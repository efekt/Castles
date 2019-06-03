package it.efekt.mc.castles.runnables;

import it.efekt.mc.castles.Castles;
import it.efekt.mc.castles.GameState;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class CastlesTimer extends BukkitRunnable {
    private boolean isActive = true;
    private long currentTime;
    private Castles castles;

    public CastlesTimer(Castles castles){
        this.castles = castles;
    }

    public void setNextState(GameState gameState){
        this.currentTime = gameState.getLength();
    }

    public void setActive(boolean isActive){
        this.isActive = isActive;
    }

    private void progress(){
        this.castles.progress();
    }

    @Override
    public void run() {
        if (!isActive){
            return;
        }
        if (currentTime <= 0){
            this.isActive = false;
            this.progress();
        }

        Bukkit.broadcastMessage("Time Left: " + currentTime + " sec, Current state: " + castles.getGameState());
        --currentTime;
    }
}
