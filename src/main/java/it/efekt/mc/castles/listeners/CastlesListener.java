package it.efekt.mc.castles.listeners;

import de.tr7zw.itemnbtapi.NBTItem;
import it.efekt.mc.castles.CastleTeam;
import it.efekt.mc.castles.Castles;
import it.efekt.mc.castles.CastlesPlugin;
import it.efekt.mc.castles.GameState;
import it.efekt.mc.castles.events.FlagBreakEvent;
import it.efekt.mc.castles.utils.CastlesUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
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

    @EventHandler
    public void onPlayerItemDrop(PlayerDropItemEvent e){
        if (!(getCastles().getGameState().equals(GameState.WAR) || getCastles().getGameState().equals(GameState.PEACE))){
            e.setCancelled(true);
            return;
        }

        NBTItem nbtItem = new NBTItem(e.getItemDrop().getItemStack());
        if (nbtItem.hasKey("castlesFlagColor")){
            e.getItemDrop().setInvulnerable(true);
            placeFlagOnGroundOrReturn(e.getItemDrop(), 5);
            ChatColor teamColor = ChatColor.valueOf(nbtItem.getString("castlesFlagColor"));
            String droppedFlagName = teamColor + getCastles().getTeam(teamColor).getName();
            Bukkit.broadcastMessage(getCastles().getPlayerTeamColor(e.getPlayer()) + e.getPlayer().getName() + ChatColor.WHITE + " dropped " + droppedFlagName);
        }
    }

    // for dropped item: returns to it's original location if exists, if not places on the ground
    private void placeFlagOnGroundOrReturn(Item flagItem, int secDelay){
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, ()->{
            CastleTeam flagTeam = getCastles().getTeamFromFlag(flagItem.getItemStack());

            // check if there is any entity that looks like our flag on the ground
            List<Entity> nearbyEntities = new ArrayList<>();
            nearbyEntities.addAll(flagItem.getLocation().getWorld().getNearbyEntities(flagItem.getBoundingBox()));

            if (!nearbyEntities.contains(flagItem)){
                return;
            }

            if (flagTeam.getFlagBlockOriginLocation() == null){
                // place block in location of itemstack in the world
                placeFlagOnGround(flagTeam, flagItem);
                flagItem.remove();
            } else{
                // place flag back onto an original location if set
                flagTeam.moveFlagToOrigin();
                flagItem.remove();
            }
        }, secDelay*20);
    }

    private void placeFlagOnGround(CastleTeam flagTeam, Item flagItem){
        Location currentItemLoc = flagItem.getLocation().getBlock().getLocation();
        currentItemLoc.getBlock().setType(Castles.FLAG);
        flagTeam.updateFlagBlockLocation(currentItemLoc);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        if (e.getDrops().stream().anyMatch(dropItem -> CastlesUtils.isFlag(dropItem))){
            dropFlagFromInventoryOnGround(player);
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
                                playerTeam.moveFlagToOrigin();
                                removeFlagFromInventory(player);
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
            // do not allow flag to be place elsewhere besides of it's original location
            if (!getCastles().getTeamFromFlag(nbtItem.getItem()).getFlagBlockOriginLocation().equals(e.getBlockPlaced().getLocation())){
                // if player tries to place a flag from different team than player's
                if (!getCastles().getTeamFromFlag(nbtItem.getItem()).equals(getCastles().getTeam(e.getPlayer()))){
                    // checking if placing next to enemy's flag

                    for (CastleTeam castleTeam : getCastles().getTeams()){
                        if (e.getBlockPlaced().getLocation().equals(castleTeam.getFlagBlockOriginLocation())){
                            continue;
                        }

                        if (castleTeam.getFlagBlockLocation() == null){
                            continue;
                        }

                        if (castleTeam.getFlagBlockOriginLocation() == null){
                            continue;
                        }
                        if (castleTeam.getFlagBlockOriginLocation() != null && castleTeam.equals(getCastles().getTeamFromFlag(nbtItem.getItem()))){
                            continue;
                        }

                        Block otherFlag = castleTeam.getFlagBlockOriginLocation().getBlock();

                        double distance = otherFlag.getLocation().distance(e.getBlockPlaced().getLocation());
                        if (distance == 1){

                            getCastles().setGameState(GameState.FINISHED);
                            getCastles().next();
                            getCastles().announceWinners(castleTeam);

                            e.setCancelled(false);
                            return;
                        }
                    }
                    e.setCancelled(true);
                    return;
                }
                e.setCancelled(true);
                return;
            } else {
                e.setCancelled(false);
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

        // if block has no team, is not a flag
        if (getCastles().getTeamFromFlag(e.getBlock()) == null){
            return;
        }

        // if destroyed flag is placed, double check
        if (!getCastles().getTeamFromFlag(e.getBlock()).isFlagPlaced()){
            return;
        }

        if (getCastles().getGameState().equals(GameState.WAR) && getCastles().getTeamFromFlag(e.getBlock()).getPlayers().contains(player)){
            e.setCancelled(true);
            player.sendMessage("You cannot dig up your own flag while in War mode");
            return;
        }

        if (getCastles().getGameState().equals(GameState.PEACE) && !getCastles().getTeamFromFlag(e.getBlock()).equals(getCastles().getTeam(player))){
            e.setCancelled(true);
            player.sendMessage("During Peace mode you cannot steal a flag!");
            return;
        }

        // prevent block from dropping natural items
        e.setDropItems(false);
        CastleTeam flagTeam = getCastles().getTeamFromFlag(e.getBlock());

        // drop it manually with pre-created item with nbt-tags
        Item droppedItem = e.getBlock().getLocation().getWorld().dropItem(e.getBlock().getLocation(), CastlesUtils.createFlag(flagTeam.getName(), flagTeam.getColor()));
        droppedItem.setInvulnerable(true);
        placeFlagOnGroundOrReturn(droppedItem, 5);

        // set flagBlockLocation as null
        flagTeam.updateFlagBlockLocation(null);

        // Calling Castles Event to separate Minecraft logic from castles logic and messaging
        FlagBreakEvent flagBreakEvent = new FlagBreakEvent(player, flagTeam, getCastles().getTeam(player));
        Bukkit.getServer().getPluginManager().callEvent(flagBreakEvent);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();

        //Drops sponge on the ground
        dropFlagFromInventoryOnGround(player);
    }

    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent e){
        if (getCastles().getTeamFromFlag(e.getBlock()) != null){
            e.setCancelled(true);
        }
    }

    //Prevent explosion
    @EventHandler
    public void onFlagExplode(EntityExplodeEvent e){
        e.blockList().removeIf(block -> getCastles().getTeamFromFlag(block) !=null);
    }

    @EventHandler
    public void onFlagEntityDamage(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof ItemStack){
            if (CastlesUtils.isFlag((ItemStack) e.getEntity())){
                e.setCancelled(true);
            }
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
    public void onPistonRetract(BlockPistonRetractEvent e){
        for (Block block : e.getBlocks()){
            if (getCastles().getTeamFromFlag(block) != null){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e){
        e.setRespawnLocation(getCastles().getConfig().getSpawnLocation());
    }

    private ItemStack removeFlagFromInventory(Player player){
        ItemStack flagItemStack = null;
        for (ItemStack item : player.getInventory().getContents()){
            if (item != null && item.getType().equals(Castles.FLAG)){
                if (CastlesUtils.isFlag(item)){
                    flagItemStack = item.clone();
                    player.getInventory().remove(item);
                }
            }
        }
        if (CastlesUtils.isFlag(player.getInventory().getItemInOffHand())){
            flagItemStack = player.getInventory().getItemInOffHand().clone();
            player.getInventory().getItemInOffHand().setType(Material.AIR);
        }
        player.updateInventory();
        return flagItemStack;
    }

    private void dropFlagFromInventoryOnGround(Player player) {
        Location loc = player.getLocation().clone().add(0, 1, 0);
        ItemStack flag = removeFlagFromInventory(player);
        Item item = player.getWorld().dropItem(loc, flag);
        item.setInvulnerable(true);
        placeFlagOnGroundOrReturn(item, 5);
        Bukkit.getLogger().log(Level.INFO, "Sponge has been dropped on the ground at loc:" + loc.toString());
    }

    private boolean isInPvpRange(Player defender){
        if (castles.getTeam(defender).getFlagBlockLocation() == null){
            return false;
        } else {
            return (defender.getLocation().distance(getCastles().getTeam(defender).getFlagBlockLocation()) <= castles.getConfig().getFlagNoPvpZoneRadius());
        }
    }
}
