package it.efekt.mc.castles.utils;

import de.tr7zw.itemnbtapi.NBTItem;
import it.efekt.mc.castles.CastleTeam;
import it.efekt.mc.castles.Castles;
import it.efekt.mc.castles.CastlesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CastlesUtils {

    public static <T> List<List<T>> partition(Collection<T> members, int maxSize)
    {
        List<List<T>> res = new ArrayList<>();

        List<T> internal = new ArrayList<>();

        for (T member : members)
        {
            internal.add(member);

            if (internal.size() == maxSize)
            {
                res.add(internal);
                internal = new ArrayList<>();
            }
        }
        if (!internal.isEmpty())
        {
            res.add(internal);
        }
        return res;
    }

    public static ChatColor getChatColor(int id){
        List<ChatColor> colors = new ArrayList<>();
        colors.add(ChatColor.BLUE);
        colors.add(ChatColor.GREEN);
        colors.add(ChatColor.GOLD);
        colors.add(ChatColor.AQUA);

        if (id > colors.size()){
            return ChatColor.WHITE;
        } else {
            return colors.get(id);
        }
    }

    public static ItemStack createFlag(String displayName, ChatColor color){
        ItemStack flagItem = new ItemStack(Castles.FLAG, 1);
        ItemMeta itemMeta = flagItem.getItemMeta();
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.setDisplayName(color + displayName);
        flagItem.setItemMeta(itemMeta);
        NBTItem nbti = new NBTItem(flagItem);
        nbti.setString(Castles.FLAG_COLOR_NBT_STRING, color.name());
        return nbti.getItem();
    }

    public static boolean isFlag(ItemStack itemStack){
        if (itemStack != null) {
            return new NBTItem(itemStack).hasKey(Castles.FLAG_COLOR_NBT_STRING);
        } else {
            return false;
        }
    }

    public static ItemStack removeFlagFromInventory(Player player){
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

    // for dropped item: returns to it's original location if exists, if not places on the ground
    public static void placeFlagOnGroundOrReturnDelayed(Item flagItem, int secDelay){
        Bukkit.getScheduler().scheduleSyncDelayedTask(CastlesPlugin.plugin, ()->{
            placeFlagOnGroundOrReturn(flagItem);
        }, secDelay*20);
    }

    public static void placeFlagOnGroundOrReturn(Item flagItem){
        CastleTeam flagTeam = CastlesPlugin.castlesManager.getInstance().getTeamFromFlag(flagItem.getItemStack());

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
    }

    public static void placeFlagOnGround(CastleTeam flagTeam, Item flagItem){
        Location currentItemLoc = flagItem.getLocation().getBlock().getLocation();
        currentItemLoc.getBlock().setType(Castles.FLAG);
        flagTeam.updateFlagBlockLocation(currentItemLoc);
    }
}
