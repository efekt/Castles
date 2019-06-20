package it.efekt.mc.castles.events;

import it.efekt.mc.castles.CastleTeam;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDropFlagEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private CastleTeam flagTeam;
    private CastleTeam playerTeam;
    private Item droppedItem;

    public PlayerDropFlagEvent(Player player, CastleTeam flagTeam, CastleTeam playerTeam, Item droppedItem) {
        this.player = player;
        this.flagTeam = flagTeam;
        this.playerTeam = playerTeam;
        this.droppedItem = droppedItem;
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

    public Item getDroppedItem() {
        return droppedItem;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}