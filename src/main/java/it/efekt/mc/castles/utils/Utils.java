package it.efekt.mc.castles.utils;

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
}
