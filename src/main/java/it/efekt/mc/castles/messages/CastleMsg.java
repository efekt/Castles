package it.efekt.mc.castles.messages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public enum CastleMsg {
    MATCH_OVER("Match is over");

    private String message;

    CastleMsg(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void broadcast(){
        Bukkit.broadcastMessage(getMessage());
    }

    public void sendMessage(Player player){
        player.sendMessage(getMessage());
    }
}
