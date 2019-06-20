package it.efekt.mc.castles.events;

import it.efekt.mc.castles.CastleTeam;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FlagReturnedToOriginEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private CastleTeam flagTeam;

    public FlagReturnedToOriginEvent(CastleTeam flagTeam) {
        this.flagTeam = flagTeam;
    }

    public CastleTeam getFlagTeam() {
        return flagTeam;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}