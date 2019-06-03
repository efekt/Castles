package it.efekt.mc.castles;

import it.efekt.mc.castles.runnables.CastlesTimer;
import it.efekt.mc.castles.utils.Utils;
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
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Castles implements Listener {
    private CastlesPlugin castlesPlugin;
    private GameState gameState = GameState.LOBBY;
    private List<CastleTeam> teams = new ArrayList<>();
    private CastlesTimer mainTimer = new CastlesTimer(this);
    private Config config;

    public Castles(CastlesPlugin plugin){
        this.castlesPlugin = plugin;
        this.config = new Config(this.castlesPlugin);
        setGameState(GameState.LOBBY);
    }

    public void start(){
        randomizeTeams(1);
        progress();
    }

    private void randomizeTeams(int teamNumber){
        List<Player> players = new ArrayList<>();
        players.addAll(Bukkit.getOnlinePlayers());

        Collections.shuffle(players);
        List<List<Player>> chunked = Utils.partition(players, players.size()/teamNumber);

        if (chunked.size() > teamNumber){
            List<Player> leftover = chunked.get(teamNumber);
            chunked.get(0).addAll(leftover);
            chunked.remove(leftover);
        }

        for (List<Player> item: chunked){
            CastleTeam team = new CastleTeam();
            team.setPlayers(item);
            this.teams.add(team);

            System.out.println("----------");
            for (Player player : item){
                System.out.println(player.getName());
            }
        }

    }



    public void progress(){
        switch (this.gameState){
            case LOBBY:
                stopCountdown();
                setGameState(GameState.PREPARATION);
                startCountdown();
                break;
            case PREPARATION:
                stopCountdown();
                setGameState(GameState.PEACE);
                startCountdown();
                break;
            case PEACE:
                stopCountdown();
                setGameState(GameState.WAR);
                startCountdown();
                break;
            case WAR:
                stopCountdown();
                Bukkit.broadcastMessage("ROUND IS OVER!");
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

    private void announceWinners(CastleTeam winnerTeam){
        Bukkit.broadcastMessage("The winners: " + winnerTeam.getPlayersAsString());
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

    private CastleTeam getTeam(Player player){
        for (CastleTeam team : this.teams){
            if (team.hasPlayer(player)){
                return team;
            }
        }
        return null;
    }

    public Config getConfig(){
        return this.config;
    }

    // Do not allow players to join while match is in progress
    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent e){
        if (!getGameState().equals(GameState.LOBBY)){
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Game in progress...");
            return;
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e){
        if (getGameState().equals(GameState.LOBBY)){
            e.getPlayer().teleport(this.config.getSpawnLocation());
            return;
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
        try {
            Material clickedBlock = block.getBlockData().getMaterial();
            if (clickedBlock.equals(Material.SPONGE) && itemInHand.equals(Material.SPONGE)) {
                setGameState(GameState.FINISHED);
                progress();
                announceWinners(getTeam(e.getPlayer()));
            }
        } catch (NullPointerException exc){}
    }
}
