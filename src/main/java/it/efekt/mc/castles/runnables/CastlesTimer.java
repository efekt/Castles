package it.efekt.mc.castles.runnables;

import it.efekt.mc.castles.Castles;
import it.efekt.mc.castles.GameState;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
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
        this.castles.next();
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
        for (Player player : Bukkit.getOnlinePlayers()){
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.DARK_AQUA + castles.getGameState().toString() + ": " + ChatColor.GOLD + currentTime));

            if (this.castles.getGameState().equals(GameState.LOBBY) || this.castles.getGameState().equals(GameState.PREPARATION)){
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.setFoodLevel(20);
            }

        }
        -- currentTime;
    }
}
