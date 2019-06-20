package it.efekt.mc.castles.listeners;

import it.efekt.mc.castles.CastleTeam;
import it.efekt.mc.castles.CastlesPlugin;
import it.efekt.mc.castles.events.PlayerBreakFlagEvent;
import it.efekt.mc.castles.events.PlayerDropFlagEvent;
import it.efekt.mc.castles.events.PlayerPickupFlagEvent;
import it.efekt.mc.castles.events.PlayerPlaceFlagEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CastlesListener implements Listener {

    public CastlesListener(CastlesPlugin plugin){
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onFlagBreak(PlayerBreakFlagEvent e){
        Player player = e.getPlayer();
        CastleTeam playerTeam = e.getPlayerTeam();
        CastleTeam flagTeam = e.getFlagTeam();
        Bukkit.broadcastMessage(playerTeam.getColor() + player.getDisplayName() + ChatColor.WHITE + " broke " + flagTeam.getColor() + flagTeam.getName());
    }

    @EventHandler
    public void onPlayerDropFlag(PlayerDropFlagEvent e){
        Player player = e.getPlayer();
        CastleTeam flagTeam = e.getFlagTeam();
        CastleTeam playerTeam = e.getPlayerTeam();
        Bukkit.broadcastMessage(playerTeam.getColor() + player.getDisplayName() + ChatColor.WHITE + " dropped " + flagTeam.getColor() + flagTeam.getName());
    }

    @EventHandler
    public void onPlayerPickupFlag(PlayerPickupFlagEvent e){
        Player player = e.getPlayer();
        CastleTeam flagTeam = e.getFlagTeam();
        CastleTeam playerTeam = e.getPlayerTeam();
        Bukkit.broadcastMessage(playerTeam.getColor() + player.getDisplayName() + ChatColor.WHITE + " picked up " + flagTeam.getColor() + flagTeam.getName());
    }

    @EventHandler
    public void onPlayerPlaceFlagEvent(PlayerPlaceFlagEvent e){
        Player player = e.getPlayer();
        CastleTeam flagTeam = e.getFlagTeam();
        CastleTeam playerTeam = e.getPlayerTeam();
        Bukkit.broadcastMessage(playerTeam.getColor() + player.getDisplayName() + ChatColor.WHITE + " placed " + flagTeam.getColor() + flagTeam.getName());
    }
}
