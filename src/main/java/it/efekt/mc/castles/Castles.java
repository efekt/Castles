package it.efekt.mc.castles;

import de.tr7zw.itemnbtapi.NBTItem;
import it.efekt.mc.castles.listeners.CastlesListener;
import it.efekt.mc.castles.runnables.CastlesTimer;
import it.efekt.mc.castles.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class Castles implements Listener {
    private CastlesPlugin plugin;
    private GameState gameState = GameState.LOBBY;
    private List<CastleTeam> teams = new ArrayList<>();
    private CastlesTimer mainTimer = new CastlesTimer(this);
    private Config config;
    private Scoreboard scoreboard;
    public static Material FLAG = Material.SPONGE;
    private final String TEAM_BASE_NAME = "Team";
    public static String FLAG_COLOR_NBT_STRING = "castlesFlagColor";

    public Castles(CastlesPlugin plugin){
        this.plugin = plugin;
        new CastlesListener(plugin, this);
        this.config = new Config(this.plugin);
        setGameState(GameState.LOBBY);
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void start(){
        randomizeTeams(this.config.getTeamCount());
        populateScoreboardTeams();
        resetAllPlayers();
        giveFlagsToPlayers();
        next();
    }

    public void giveFlagsToPlayers(){
        for (CastleTeam team : this.teams){
            Team scoreboardTeam = scoreboard.getPlayerTeam(Bukkit.getOfflinePlayer(team.getPlayers().get(0).getUniqueId()));
            team.getPlayers().get(0).getInventory().addItem(createFlag(scoreboardTeam.getDisplayName(), scoreboardTeam.getColor()));
        }
    }

    public void resetAllPlayers(){
        for (Player player : Bukkit.getOnlinePlayers()){
            player.setSaturation(20f);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.getInventory().clear();
            player.updateInventory();
        }
    }

    public void randomizeTeams(int teamNumber){
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

    public void populateScoreboardTeams(){
        int i = 1;
        Objective objective = this.scoreboard.registerNewObjective("totalKillCount ", "totalKillCount ");
        objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        for (CastleTeam castleTeam : this.teams){
            String teamName = TEAM_BASE_NAME + " " + i;
            Team team = scoreboard.registerNewTeam(teamName);
            team.setColor(Utils.getChatColor(i-1));
            team.setDisplayName(teamName);
            castleTeam.setScoreboardTeam(team);
            for (Player player : castleTeam.getPlayers()){
               team.addPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
               player.setScoreboard(this.scoreboard);
            }
            i++;
        }
    }

    public void next(){
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
                placeAllFlags();
                setAllFlagsOriginLocations();
                startCountdown();
                break;
            case WAR:
                stopCountdown();
                Bukkit.broadcastMessage("ROUND IS OVER!");
                setGameState(GameState.FINISHED);
                break;
            case FINISHED:
                stopCountdown();
                Bukkit.broadcastMessage("This is the end!");
                break;
            default:
                break;
        }
    }

    public List<CastleTeam> getTeams(){
        return this.teams;
    }

    public void announceWinners(CastleTeam winnerTeam){
        Bukkit.broadcastMessage("The winners: " + winnerTeam.getPlayersAsString());
    }

    public void startCountdown(){
        this.mainTimer = new CastlesTimer(this);
        this.mainTimer.setNextState(this.gameState);
        this.mainTimer.runTaskTimer(this.plugin, 0L, 20L);
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

    public CastleTeam getTeam(Player player){
            for (CastleTeam team : this.teams) {
                if (team.hasPlayer(player)) {
                    return team;
                }
            }
        return null;
    }

    public CastleTeam getTeam(ChatColor color){
        for (CastleTeam team : this.teams){
            if (team.getColor() != null && team.getColor().name().equalsIgnoreCase(color.name())){
                return team;
            }
        }
        return null;
    }

    public void setAllFlagsOriginLocations(){
        for (CastleTeam team : getTeams()){
            team.setFlagBlockOriginLocation(team.getFlagBlockLocation());
        }
    }

    public boolean isInTeam(Player player){
        return isInTeam(player.getUniqueId().toString());
    }

    public boolean isInTeam(String uuid){
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

    public ChatColor getPlayerTeamColor(Player player){
        return this.scoreboard.getPlayerTeam(Bukkit.getOfflinePlayer(player.getUniqueId())).getColor();
    }

    public CastleTeam getTeamFromFlag(Block block){
        CastleTeam team = null;
            for (CastleTeam castleTeam : this.teams) {
                if (castleTeam.isFlagPlaced()){
                    if (castleTeam.getFlagBlockLocation() != null){
                        if (castleTeam.getFlagBlockLocation().equals(block.getLocation())){
                            team = castleTeam;
                        }
                    }
                }
            }
        return team;
    }

    public CastleTeam getTeamFromFlag(ItemStack itemStack){
        if (isFlag(itemStack)){
            return getTeam(ChatColor.valueOf(new NBTItem(itemStack).getString(FLAG_COLOR_NBT_STRING)));
        }
        return null;
    }

    public void placeAllFlags(){
        for (CastleTeam team : this.teams){
            for (Player player : team.getPlayers()){
                for (ItemStack item : player.getInventory().getContents()){
                    if (item != null && item.getType().equals(FLAG)){
                        if (isFlag(item)){
                            Location loc = player.getLocation().getBlock().getLocation();
                            loc.getBlock().setType(item.getType());
                            player.getInventory().remove(item);
                            player.updateInventory();
                            CastleTeam castleTeam = getTeamFromFlag(item);
                            castleTeam.updateFlagBlockLocation(loc);
                            Bukkit.getLogger().log(Level.INFO, "Player " + player.getName() + " didn't place flag in Peace time, placed automatically at loc:" + loc.toString());
                        }
                    }
                }
            }
        }
    }

    public Scoreboard getScoreboard(){
            return this.scoreboard;
    }

    public ItemStack createFlag(String displayName, ChatColor color){
        ItemStack flagItem = new ItemStack(FLAG, 1);
        ItemMeta itemMeta = flagItem.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.setDisplayName(color + displayName);
        flagItem.setItemMeta(itemMeta);
        NBTItem nbti = new NBTItem(flagItem);
        nbti.setString(FLAG_COLOR_NBT_STRING, color.name());
        return nbti.getItem();
    }

    public boolean isFlag(ItemStack itemStack){
        return new NBTItem(itemStack).hasKey(FLAG_COLOR_NBT_STRING);
    }
}
