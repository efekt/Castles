package it.efekt.mc.castles;

import it.efekt.mc.castles.runnables.CastlesTimer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class Castles implements Listener {
    private CastlesPlugin castlesPlugin;
    private GameState gameState = GameState.LOBBY;
    private List<CastleTeam> teams = new ArrayList<>();
    private CastlesTimer mainTimer = new CastlesTimer(this);

    public Castles(CastlesPlugin plugin){
        this.castlesPlugin = plugin;
    }

    public void start(){
        progress();
    }


    public void progress(){
        switch (this.gameState){

            case LOBBY:
                setGameState(GameState.PREPARATION);
                startCountdown();
                break;
            case PREPARATION:
                setGameState(GameState.PEACE);
                startCountdown();
                break;
            case PEACE:
                setGameState(GameState.WAR);
                startCountdown();
                break;
            case WAR:
                setGameState(GameState.FINISHED);
                break;
            case FINISHED:
                stopCountdown();
                Bukkit.broadcastMessage("IT IS OVER!");
                break;
            default:
                break;
        }
    }

    public void startCountdown(){
        this.mainTimer = new CastlesTimer(this);
        this.mainTimer.setNextState(this.gameState);
        this.mainTimer.runTaskTimer(this.castlesPlugin, 0L, 20L);
    }

    public void stopCountdown(){
        this.mainTimer.setActive(false);
    }

    public void setGameState(GameState gameState){
        this.gameState = gameState;
    }

    public GameState getGameState(){
        return this.gameState;
    }

    // Do not allow players to join while match is in progress
    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent e){
        if (!getGameState().equals(GameState.LOBBY)){
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Game in progress...");
        }
    }

    // Disable any entity damage if game is not in a war mode
    @EventHandler
    public void onPlayerAttack(EntityDamageEvent e){
        if (!getGameState().equals(GameState.WAR) && (e.getEntity() instanceof Player)){
            e.setCancelled(true);
        }
    }

    // Disable digging while not in peace or war modes
    @EventHandler
    public void onPlayerDig(BlockBreakEvent e){
        if (gameState.equals(GameState.WAR) || gameState.equals(GameState.PEACE)){
            e.setCancelled(false);
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onSpongePlace(PlayerInteractEvent e){
        if (getGameState().equals(GameState.FINISHED)){
            return;
        }
        Block block = e.getClickedBlock();
        Material itemInHand = e.getPlayer().getInventory().getItemInMainHand().getType();
        Material clickedBlock = block.getBlockData().getMaterial();
        if (clickedBlock.equals(Material.SPONGE) && itemInHand.equals(Material.SPONGE)){
            setGameState(GameState.FINISHED);
            progress();
        }
    }
}
