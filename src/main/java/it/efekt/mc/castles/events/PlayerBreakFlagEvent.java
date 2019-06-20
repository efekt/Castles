package it.efekt.mc.castles.events;

import it.efekt.mc.castles.CastleTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerBreakFlagEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private CastleTeam playerTeam;
    private CastleTeam flagTeam;

    public PlayerBreakFlagEvent(Player player, CastleTeam flagTeam, CastleTeam playerTeam){
        this.player = player;
        this.flagTeam = flagTeam;
        this.playerTeam = playerTeam;
    }

    public Player getPlayer() {
        return player;
    }

    public CastleTeam getFlagTeam() {
        return flagTeam;
    }

    public CastleTeam getPlayerTeam() {
        return playerTeam;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList(){
        return HANDLERS;
    }
}
