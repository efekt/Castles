package it.efekt.mc.castles;

import de.tr7zw.itemnbtapi.NBTItem;
import it.efekt.mc.castles.events.FlagBreakEvent;
import it.efekt.mc.castles.listeners.CastlesListener;
import it.efekt.mc.castles.runnables.CastlesTimer;
import it.efekt.mc.castles.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class Castles implements Listener {
    private CastlesPlugin plugin;
    private CastlesListener castlesListener;
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
        this.castlesListener = new CastlesListener(plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

    private void giveFlagsToPlayers(){
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

    private void announceWinners(CastleTeam winnerTeam){
        Bukkit.broadcastMessage("The winners: " + winnerTeam.getPlayersAsString());
    }

    private void startCountdown(){
        this.mainTimer = new CastlesTimer(this);
        this.mainTimer.setNextState(this.gameState);
        this.mainTimer.runTaskTimer(this.plugin, 0L, 20L);
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
            if (team.getColor() != null && team.getColor().name().equalsIgnoreCase(color.name())){
                return team;
            }
        }
        return null;
    }

    private boolean isInTeam(Player player){
        return isInTeam(player.getUniqueId().toString());
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

    private CastleTeam getTeamFromFlag(Block block){
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

    private CastleTeam getTeamFromFlag(ItemStack itemStack){
        if (isFlag(itemStack)){
            return getTeam(ChatColor.valueOf(new NBTItem(itemStack).getString(FLAG_COLOR_NBT_STRING)));
        }
        return null;
    }

    private void placeAllFlags(){
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

    private void setAllFlagsOriginLocations(){
        for (CastleTeam team : this.teams){
            team.setFlagBlockOriginLocation(team.getFlagBlockLocation());
        }
    }



    // Do not allow players to join while match is in progress
    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e){
        if (!getGameState().equals(GameState.LOBBY)){
            if (!isInTeam(e.getUniqueId().toString())){
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Game in progress...");
            }
            return;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if (getGameState().equals(GameState.LOBBY)){
            e.getPlayer().teleport(this.config.getSpawnLocation());
        }

        if (isInTeam(e.getPlayer())){
            e.getPlayer().setScoreboard(this.scoreboard);
        }
    }

    // Disable any entity damage if game is not in a war mode
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        // if Player attacks Player
        if (((e.getEntity() instanceof Player) && (e.getDamager() instanceof Player))){
            if (!getGameState().equals(GameState.WAR) && !getGameState().equals(GameState.PEACE)) {
                e.setCancelled(true);
            }

            if (gameState.equals(GameState.PEACE)){
                Player attacker = (Player) e.getDamager();
                Player defender = (Player) e.getEntity();

                if (!getTeam(defender).isFlagPlaced()){
                    return;
                }

                Location defenderFlagOrigin = getTeam(defender).getFlagBlockLocation();

                int pvpZoneRadius = getConfig().getFlagNoPvpZoneRadius();

                if (defender.getLocation().distance(defenderFlagOrigin) <= pvpZoneRadius){
                    attacker.sendMessage(defender.getDisplayName() + " is protected in this area");
                    e.setCancelled(true);
                } else {
                    e.setCancelled(false);
                }
            }


        }
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
                next();
                announceWinners(getTeam(e.getPlayer()));
            }
        } catch (NullPointerException exc){}
    }

    @EventHandler
    public void onPlayerItemDrop(PlayerDropItemEvent e){
        if (!(getGameState().equals(GameState.WAR) || getGameState().equals(GameState.PEACE))){
            e.setCancelled(true);
            return;
        }


        ItemStack itemStack = e.getItemDrop().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("castlesFlagColor")){
            ChatColor teamColor = ChatColor.valueOf(nbtItem.getString("castlesFlagColor"));
            String droppedFlagName = teamColor + getTeam(teamColor).getName();

            Bukkit.broadcastMessage(getPlayerTeamColor(e.getPlayer()) + e.getPlayer().getName() + ChatColor.WHITE + " dropped " + droppedFlagName);
        }
    }

    @EventHandler
    public void onPlayerFlagPickup(EntityPickupItemEvent e){
        if (e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();

            NBTItem nbtItem = new NBTItem(e.getItem().getItemStack());
            if (nbtItem.hasKey(FLAG_COLOR_NBT_STRING)){
                ChatColor teamColor = ChatColor.valueOf(nbtItem.getString(FLAG_COLOR_NBT_STRING));
                String droppedFlagName = teamColor + getTeam(teamColor).getName();

                Bukkit.broadcastMessage(getPlayerTeamColor(player) + player.getName() + ChatColor.WHITE + " picked up " + droppedFlagName);

                if (getGameState().equals(GameState.WAR)){

                    ItemStack pickedUpFlag = nbtItem.getItem();
                    CastleTeam flagTeam = getTeamFromFlag(pickedUpFlag);
                    CastleTeam playerTeam = getTeam(player);

                    if (!flagTeam.equals(playerTeam)){
                        return;
                    }

                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, ()->{
                        for (ItemStack itemStack : player.getInventory().getContents()){
                            if (itemStack != null && itemStack.equals(pickedUpFlag)){
                                playerTeam.getFlagBlockOriginLocation().getBlock().setType(FLAG);
                                playerTeam.updateFlagBlockLocation(playerTeam.getFlagBlockOriginLocation());
                                player.getInventory().remove(itemStack);
                                player.updateInventory();
                                return;
                            }
                        }

                    }, 100);
                }

            }
        }
    }

    @EventHandler
    public void onPlayerFlagPlace(BlockPlaceEvent e){
        if (!(getGameState().equals(GameState.WAR) || getGameState().equals(GameState.PEACE))){
            e.setCancelled(true);
            return;
        }


        NBTItem nbtItem = new NBTItem(e.getItemInHand());
        if (!nbtItem.hasKey(FLAG_COLOR_NBT_STRING)){
            return;
        }

        if (getGameState().equals(GameState.WAR)){
            if (!getTeam(e.getPlayer()).getFlagBlockOriginLocation().equals(e.getBlockPlaced().getLocation())){
                e.setCancelled(true);
                return;
            }
        }

        ChatColor teamColor = ChatColor.valueOf(nbtItem.getString(FLAG_COLOR_NBT_STRING));

        CastleTeam castleTeam = getTeam(teamColor);
        Location blockLocation = e.getBlockPlaced().getLocation();
        castleTeam.updateFlagBlockLocation(blockLocation);
        Bukkit.broadcastMessage(getPlayerTeamColor(e.getPlayer()) + e.getPlayer().getName() + ChatColor.WHITE + " placed " + castleTeam.getColor() + castleTeam.getName());
    }

    @EventHandler
    public void onPlayerFlagBreak(BlockBreakEvent e){
        Player player = e.getPlayer();

        if (!(gameState.equals(GameState.WAR) || gameState.equals(GameState.PEACE))){
            e.setCancelled(true);
            return;
        }


        if (getTeamFromFlag(e.getBlock()) == null){
            return;
        }

        if (!getTeamFromFlag(e.getBlock()).isFlagPlaced()){
            return;
        }

        if (getGameState().equals(GameState.WAR) && getTeamFromFlag(e.getBlock()).getPlayers().contains(player)){
            player.sendMessage("You cannot dig up your own flag while in War mode");
            e.setCancelled(true);
            return;
        }

        // prevent block from dropping natural items
        e.setDropItems(false);
        CastleTeam flagTeam = getTeamFromFlag(e.getBlock());
        // drop it manually with pre-created item with nbt-tags
        e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), createFlag(flagTeam.getName(), flagTeam.getColor()));
        // set flagBlockLocation as null
        flagTeam.updateFlagBlockLocation(null);

        // Calling Castles Event to separate Minecraft logic from castles logic and messaging
        FlagBreakEvent flagBreakEvent = new FlagBreakEvent(player, flagTeam, getTeam(player));
        Bukkit.getServer().getPluginManager().callEvent(flagBreakEvent);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
       Player player = e.getPlayer();
       System.out.println(player.getInventory().getContents().length);


       //Drops sponge on the ground
       for (ItemStack item : player.getInventory().getContents()){
           if (item != null && item.getType().equals(FLAG)){
               if (isFlag(item)){
                   Location loc = player.getLocation().clone().add(0,1,0);
                   player.getWorld().dropItem(loc, item);
                   player.getInventory().remove(item);
                   player.updateInventory();
                   Bukkit.getLogger().log(Level.INFO, "Player " + player.getName() + " left the server with sponge, sponge has been dropped on the ground at loc:" + loc.toString());
               }
           }
       }
    }

    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent e){
        if (getTeamFromFlag(e.getBlock()) != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFlagExplode(BlockExplodeEvent e){
        if (getTeamFromFlag(e.getBlock()) != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e){
        for (Block block : e.getBlocks()){
            if (getTeamFromFlag(block) != null){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonRetractEvent e){
        for (Block block : e.getBlocks()){
            if (getTeamFromFlag(block) != null){
                e.setCancelled(true);
            }
        }
    }





    private ItemStack createFlag(String displayName, ChatColor color){
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

    private boolean isFlag(ItemStack itemStack){
        return new NBTItem(itemStack).hasKey(FLAG_COLOR_NBT_STRING);
    }

}
