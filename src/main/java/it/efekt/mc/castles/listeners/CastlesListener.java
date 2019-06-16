package it.efekt.mc.castles.listeners;

import it.efekt.mc.castles.CastleTeam;
import it.efekt.mc.castles.CastlesPlugin;
import it.efekt.mc.castles.events.FlagBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CastlesListener implements Listener {
    private CastlesPlugin plugin;

    public CastlesListener(CastlesPlugin plugin){
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }


    @EventHandler
    public void FlagBreakEvent(FlagBreakEvent e){
        Player player = e.getPlayer();
        CastleTeam playerTeam = e.getPlayerTeam();
        CastleTeam flagTeam = e.getFlagTeam();
        Bukkit.broadcastMessage(playerTeam.getColor() + e.getPlayer().getName() + ChatColor.WHITE + " broke " + flagTeam.getColor() + flagTeam.getName());

    }
}
