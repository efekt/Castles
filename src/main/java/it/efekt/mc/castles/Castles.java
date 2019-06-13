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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Castles implements Listener {
    private CastlesPlugin castlesPlugin;
    private GameState gameState = GameState.LOBBY;
    private List<CastleTeam> teams = new ArrayList<>();
    private CastlesTimer mainTimer = new CastlesTimer(this);
    private Config config;
    private Scoreboard scoreboard;
    private final Material FLAG = Material.SPONGE;

    public Castles(CastlesPlugin plugin){
        this.castlesPlugin = plugin;
        this.config = new Config(this.castlesPlugin);
        setGameState(GameState.LOBBY);
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void start(){
        randomizeTeams(this.config.getTeamCount());
        populateScoreboardTeams();
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

    private void populateScoreboardTeams(){
        int i = 0;
        Objective objective = this.scoreboard.registerNewObjective("totalKillCount ", "totalKillCount ");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        for (CastleTeam castleTeam : this.teams){
            String teamName = "Team " + i;
            Team team = scoreboard.registerNewTeam(teamName);
            team.setColor(Utils.getChatColor(i));
            team.setDisplayName(teamName);
            for (Player player : castleTeam.getPlayers()){
               team.addPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
               player.setScoreboard(this.scoreboard);
            }
            i++;
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

    public List<CastleTeam> getTeams(){
        return this.teams;
    }

    private void announceWinners(CastleTeam winnerTeam){
        Bukkit.broadcastMessage("The winners: " + winnerTeam.getPlayersAsString());
    }

    private void startCountdown(){
        this.mainTimer = new CastlesTimer(this);
        this.mainTimer.setNextState(this.gameState);
        this.mainTimer.runTaskTimer(this.castlesPlugin, 0L, 20L);
    }

    private void stopCountdown(){
        this.mainTimer.setActive(false);
    }

    private void setGameState(GameState gameState){
        this.gameState = gameState;
    }

    public GameState getGameState(){
        return this.gameState;
    }

    private CastleTeam getTeam(Player player){
            for (CastleTeam team : this.teams) {
                if (team.hasPlayer(player)) {
                    return team;
                }
            }
        return null;
    }

    private boolean isInTeam(String uuid){
        for (CastleTeam team : this.teams){
            if (team.hasPlayerUUID(uuid)){
                return true;
            }
        }
        return false;
    }

    public Config getConfig(){
        return this.config;
    }

    // Do not allow players to join while match is in progress
    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent e){
        if (!getGameState().equals(GameState.LOBBY)){
            if (!isInTeam(e.getUniqueId().toString())){
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Game in progress...");
            }
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
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        if (!getGameState().equals(GameState.WAR) && ((e.getEntity() instanceof Player) && (e.getDamager() instanceof Player))){
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
            if (clickedBlock.equals(FLAG) && itemInHand.equals(FLAG)) {
                setGameState(GameState.FINISHED);
                progress();
                announceWinners(getTeam(e.getPlayer()));
            }
        } catch (NullPointerException exc){}
    }
}
