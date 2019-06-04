package it.efekt.mc.castles.utils;

import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {

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
        if (internal.isEmpty() == false)
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
}
