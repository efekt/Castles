package it.efekt.mc.castles.events;

import it.efekt.mc.castles.CastleTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameFinishedEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player whoPlacedFlag;
    private CastleTeam winnerTeam;

    public GameFinishedEvent(Player whoPlacedFlag, CastleTeam winnerTeam) {
        this.whoPlacedFlag = whoPlacedFlag;
        this.winnerTeam = winnerTeam;
    }

    public Player getWhoPlacedFlag() {
        return whoPlacedFlag;
    }

    public CastleTeam getWinnerTeam() {
        return winnerTeam;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}