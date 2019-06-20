package it.efekt.mc.castles.events;

import it.efekt.mc.castles.CastleTeam;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPlaceFlagEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private CastleTeam flagTeam;
    private CastleTeam playerTeam;
    private Block placedBlock;

    public PlayerPlaceFlagEvent(Player player, CastleTeam flagTeam, CastleTeam playerTeam, Block placedBlock) {
        this.player = player;
        this.flagTeam = flagTeam;
        this.playerTeam = playerTeam;
        this.placedBlock = placedBlock;
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

    public Block getPlacedBlock() {
        return placedBlock;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}