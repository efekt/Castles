package it.efekt.mc.castles.utils;

import de.tr7zw.itemnbtapi.NBTItem;
import it.efekt.mc.castles.Castles;
import org.bukkit.ChatColor;
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
}
