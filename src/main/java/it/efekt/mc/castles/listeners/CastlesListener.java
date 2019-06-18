package it.efekt.mc.castles.listeners;

import de.tr7zw.itemnbtapi.NBTItem;
import it.efekt.mc.castles.CastleTeam;
import it.efekt.mc.castles.Castles;
import it.efekt.mc.castles.CastlesPlugin;
import it.efekt.mc.castles.GameState;
import it.efekt.mc.castles.events.FlagBreakEvent;
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
import org.bukkit.inventory.ItemStack;
import java.util.logging.Level;

public class CastlesListener implements Listener {
    private CastlesPlugin plugin;
    private Castles castles;

    public CastlesListener(CastlesPlugin plugin, Castles castles){
        this.plugin = plugin;
        this.castles = castles;
        plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    private Castles getCastles(){
        return this.castles;
    }

    @EventHandler
    public void FlagBreakEvent(FlagBreakEvent e){
        CastleTeam playerTeam = e.getPlayerTeam();
        CastleTeam flagTeam = e.getFlagTeam();
        Bukkit.broadcastMessage(playerTeam.getColor() + e.getPlayer().getName() + ChatColor.WHITE + " broke " + flagTeam.getColor() + flagTeam.getName());

    }

    // Do not allow players to join while match is in progress
    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e){
        if (!getCastles().getGameState().equals(GameState.LOBBY)){
            if (!getCastles().isInTeam(e.getUniqueId().toString())){
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Game in progress...");
            }
            return;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if (getCastles().getGameState().equals(GameState.LOBBY)){
            e.getPlayer().teleport(getCastles().getConfig().getSpawnLocation());
        }

        if (getCastles().isInTeam(e.getPlayer())){
            e.getPlayer().setScoreboard(getCastles().getScoreboard());
        }
    }

    // Disable any entity damage if game is not in a war mode
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e){
        // if Player attacks Player
        if (((e.getEntity() instanceof Player) && (e.getDamager() instanceof Player))){
            if (!getCastles().getGameState().equals(GameState.WAR) && !getCastles().getGameState().equals(GameState.PEACE)) {
                e.setCancelled(true);
            }

            if (getCastles().getGameState().equals(GameState.PEACE)){
                Player attacker = (Player) e.getDamager();
                Player defender = (Player) e.getEntity();

                if (getCastles().getTeam(attacker).isFlagPlaced()){
                    if (isInPvpRange(defender)){
                        attacker.sendMessage(defender.getDisplayName() + " is protected in this area");
                        e.setCancelled(true);
                    } else {
                        e.setCancelled(false);
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    private boolean isInPvpRange(Player defender){
        if (castles.getTeam(defender).getFlagBlockLocation() == null){
            return false;
        } else {
            return (defender.getLocation().distance(getCastles().getTeam(defender).getFlagBlockLocation()) <= castles.getConfig().getFlagNoPvpZoneRadius());
        }
    }

    @EventHandler
    public void onSpongePlace(PlayerInteractEvent e){
        if (getCastles().getGameState().equals(GameState.FINISHED)){
            return;
        }
        Block block = e.getClickedBlock();
        Material itemInHand = e.getPlayer().getInventory().getItemInMainHand().getType();
        try {
            Material clickedBlock = block.getBlockData().getMaterial();
            if (clickedBlock.equals(Castles.FLAG) && itemInHand.equals(Castles.FLAG)) {
                getCastles().setGameState(GameState.FINISHED);
                getCastles().next();
                getCastles().announceWinners(getCastles().getTeam(e.getPlayer()));
            }
        } catch (NullPointerException exc){}
    }

    @EventHandler
    public void onPlayerItemDrop(PlayerDropItemEvent e){
        if (!(getCastles().getGameState().equals(GameState.WAR) || getCastles().getGameState().equals(GameState.PEACE))){
            e.setCancelled(true);
            return;
        }


        ItemStack itemStack = e.getItemDrop().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("castlesFlagColor")){
            ChatColor teamColor = ChatColor.valueOf(nbtItem.getString("castlesFlagColor"));
            String droppedFlagName = teamColor + getCastles().getTeam(teamColor).getName();

            Bukkit.broadcastMessage(getCastles().getPlayerTeamColor(e.getPlayer()) + e.getPlayer().getName() + ChatColor.WHITE + " dropped " + droppedFlagName);
        }
    }

    @EventHandler
    public void onPlayerFlagPickup(EntityPickupItemEvent e){
        if (e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();

            NBTItem nbtItem = new NBTItem(e.getItem().getItemStack());
            if (nbtItem.hasKey(Castles.FLAG_COLOR_NBT_STRING)){
                ChatColor teamColor = ChatColor.valueOf(nbtItem.getString(Castles.FLAG_COLOR_NBT_STRING));
                String droppedFlagName = teamColor + getCastles().getTeam(teamColor).getName();

                Bukkit.broadcastMessage(getCastles().getPlayerTeamColor(player) + player.getName() + ChatColor.WHITE + " picked up " + droppedFlagName);

                if (getCastles().getGameState().equals(GameState.WAR)){

                    ItemStack pickedUpFlag = nbtItem.getItem();
                    CastleTeam flagTeam = getCastles().getTeamFromFlag(pickedUpFlag);
                    CastleTeam playerTeam = getCastles().getTeam(player);

                    if (!flagTeam.equals(playerTeam)){
                        return;
                    }

                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, ()->{
                        for (ItemStack itemStack : player.getInventory().getContents()){
                            if (itemStack != null && itemStack.equals(pickedUpFlag)){
                                playerTeam.getFlagBlockOriginLocation().getBlock().setType(Castles.FLAG);
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
        if (!(getCastles().getGameState().equals(GameState.WAR) || getCastles().getGameState().equals(GameState.PEACE))){
            e.setCancelled(true);
            return;
        }


        NBTItem nbtItem = new NBTItem(e.getItemInHand());
        if (!nbtItem.hasKey(Castles.FLAG_COLOR_NBT_STRING)){
            return;
        }

        if (getCastles().getGameState().equals(GameState.WAR)){
            if (!getCastles().getTeam(e.getPlayer()).getFlagBlockOriginLocation().equals(e.getBlockPlaced().getLocation())){
                e.setCancelled(true);
                return;
            }
        }

        ChatColor teamColor = ChatColor.valueOf(nbtItem.getString(Castles.FLAG_COLOR_NBT_STRING));

        CastleTeam castleTeam = getCastles().getTeam(teamColor);
        Location blockLocation = e.getBlockPlaced().getLocation();
        castleTeam.updateFlagBlockLocation(blockLocation);
        Bukkit.broadcastMessage(getCastles().getPlayerTeamColor(e.getPlayer()) + e.getPlayer().getName() + ChatColor.WHITE + " placed " + castleTeam.getColor() + castleTeam.getName());
    }

    @EventHandler
    public void onPlayerFlagBreak(BlockBreakEvent e){
        Player player = e.getPlayer();

        if (!(getCastles().getGameState().equals(GameState.WAR) || getCastles().getGameState().equals(GameState.PEACE))){
            e.setCancelled(true);
            return;
        }


        if (getCastles().getTeamFromFlag(e.getBlock()) == null){
            return;
        }

        if (!getCastles().getTeamFromFlag(e.getBlock()).isFlagPlaced()){
            return;
        }

        if (getCastles().getGameState().equals(GameState.WAR) && getCastles().getTeamFromFlag(e.getBlock()).getPlayers().contains(player)){
            player.sendMessage("You cannot dig up your own flag while in War mode");
            e.setCancelled(true);
            return;
        }

        // prevent block from dropping natural items
        e.setDropItems(false);
        CastleTeam flagTeam = getCastles().getTeamFromFlag(e.getBlock());
        // drop it manually with pre-created item with nbt-tags
        e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), getCastles().createFlag(flagTeam.getName(), flagTeam.getColor()));
        // set flagBlockLocation as null
        flagTeam.updateFlagBlockLocation(null);

        // Calling Castles Event to separate Minecraft logic from castles logic and messaging
        FlagBreakEvent flagBreakEvent = new FlagBreakEvent(player, flagTeam, getCastles().getTeam(player));
        Bukkit.getServer().getPluginManager().callEvent(flagBreakEvent);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        System.out.println(player.getInventory().getContents().length);


        //Drops sponge on the ground
        for (ItemStack item : player.getInventory().getContents()){
            if (item != null && item.getType().equals(Castles.FLAG)){
                if (getCastles().isFlag(item)){
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
        if (getCastles().getTeamFromFlag(e.getBlock()) != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFlagExplode(BlockExplodeEvent e){
        if (getCastles().getTeamFromFlag(e.getBlock()) != null){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e){
        for (Block block : e.getBlocks()){
            if (getCastles().getTeamFromFlag(block) != null){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonRetractEvent e){
        for (Block block : e.getBlocks()){
            if (getCastles().getTeamFromFlag(block) != null){
                e.setCancelled(true);
            }
        }
    }

}
