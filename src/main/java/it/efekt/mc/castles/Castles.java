package it.efekt.mc.castles;

import de.tr7zw.itemnbtapi.NBTItem;
import it.efekt.mc.castles.runnables.CastlesTimer;
import it.efekt.mc.castles.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    private final String TEAM_BASE_NAME = "Team";

    public Castles(CastlesPlugin plugin){
        this.castlesPlugin = plugin;
        this.config = new Config(this.castlesPlugin);
        setGameState(GameState.LOBBY);
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void start(){
        randomizeTeams(this.config.getTeamCount());
        populateScoreboardTeams();
        resetAllPlayers();
        giveFlagsItems();
        progress();
    }

    private void giveFlagsItems(){
        for (CastleTeam team : this.teams){
            Team scoreboardTeam = scoreboard.getPlayerTeam(Bukkit.getOfflinePlayer(team.getPlayers().get(0).getUniqueId()));
            team.getPlayers().get(0).getInventory().addItem(createFlag(scoreboardTeam.getDisplayName(), scoreboardTeam.getColor()));
        }
    }

    private void resetAllPlayers(){
        for (Player player : Bukkit.getOnlinePlayers()){
            player.setSaturation(20f);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.getInventory().clear();
            player.updateInventory();
        }
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

    private CastleTeam getTeam(ChatColor color){
        for (CastleTeam team : this.teams){
            if (team.getFlagColor() != null && team.getFlagColor().name().equalsIgnoreCase(color.name())){
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

    private ChatColor getPlayerTeamColor(Player player){
        return this.scoreboard.getPlayerTeam(Bukkit.getOfflinePlayer(player.getUniqueId())).getColor();
    }

    private CastleTeam getTeamFromFlag(Location location){
        CastleTeam team = null;
            for (CastleTeam castleTeam : this.teams) {
                if (castleTeam.isFlagPlaced()){
                    if (castleTeam.getFlagBlockLocation() != null){
                        if (castleTeam.getFlagBlockLocation().equals(location)){
                            team = castleTeam;
                        }
                    }
                }
            }
        return team;
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

    @EventHandler (priority = EventPriority.HIGHEST)
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

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerItemDrop(PlayerDropItemEvent e){
        ItemStack itemStack = e.getItemDrop().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("castlesFlagId")){
            String droppedFlagName = "" + ChatColor.valueOf(nbtItem.getString("castlesFlagColor")) + (nbtItem.getString("castlesFlagId"));

            Bukkit.broadcastMessage(getPlayerTeamColor(e.getPlayer()) + e.getPlayer().getName() + ChatColor.WHITE + " dropped " + droppedFlagName);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerFlagPickup(EntityPickupItemEvent e){
        if (e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();

            NBTItem nbtItem = new NBTItem(e.getItem().getItemStack());
            if (nbtItem.hasKey("castlesFlagId")){
                String droppedFlagName = "" + ChatColor.valueOf(nbtItem.getString("castlesFlagColor")) + (nbtItem.getString("castlesFlagId"));

                Bukkit.broadcastMessage(getPlayerTeamColor(player) + player.getName() + ChatColor.WHITE + " picked up " + droppedFlagName);
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerFlagPlace(BlockPlaceEvent e){

        NBTItem nbtItem = new NBTItem(e.getItemInHand());
        if (!nbtItem.hasKey("castlesFlagId")){
            return;
        }

        ChatColor teamColor = ChatColor.valueOf(nbtItem.getString("castlesFlagColor"));

        CastleTeam castleTeam = getTeam(teamColor);
        Location blockLocation = e.getBlockPlaced().getLocation();
        castleTeam.updateFlagBlock(blockLocation);
        Bukkit.broadcastMessage(getPlayerTeamColor(e.getPlayer()) + e.getPlayer().getName() + ChatColor.WHITE + " placed " + castleTeam.getFlagColor() + castleTeam.getFlagName());
        System.out.println(castleTeam.getFlagBlockLocation().toString());
        System.out.println("after placed: " + getTeamFromFlag(e.getBlockPlaced().getLocation()).getFlagName());
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerFlagBreak(BlockBreakEvent e){
        System.out.println(e.getBlock().getLocation().toString());

        if (!getTeamFromFlag(e.getBlock().getLocation()).isFlagPlaced()){
            return;
        }

        e.setDropItems(false);
        CastleTeam castleTeam = getTeamFromFlag(e.getBlock().getLocation());
        System.out.println(castleTeam.getFlagBlockLocation().toString());
        e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), createFlag(castleTeam.getFlagName(), castleTeam.getFlagColor()));
        Bukkit.broadcastMessage(getPlayerTeamColor(e.getPlayer()) + e.getPlayer().getName() + ChatColor.WHITE + " broke " + castleTeam.getFlagColor() + castleTeam.getFlagName());
        castleTeam.updateFlagBlock(null);
    }

    private ItemStack createFlag(String displayName, ChatColor color){
        ItemStack flagItem = new ItemStack(this.FLAG, 1);
        ItemMeta itemMeta = flagItem.getItemMeta();
        itemMeta.setDisplayName(color + displayName);
        flagItem.setItemMeta(itemMeta);
        NBTItem nbti = new NBTItem(flagItem);
        nbti.setString("castlesFlagId", displayName);
        nbti.setString("castlesFlagColor", color.name());
        return nbti.getItem();
    }

}
