package it.efekt.mc.castles;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class CastleTeam {
    private List<Player> players = new ArrayList<>();
    private String name;


    public boolean hasPlayer(Player player){
        return this.players.contains(player);
    }

    public void addPlayer(Player player){
        this.players.add(player);
    }

    public void removePlayer(Player player){
        if (hasPlayer(player)){
            this.players.remove(player);
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public String getPlayersAsString(){
        String players = "";
        for (Player player : this.players){
            players = players.concat(player.getName() + " ");
        }

        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}
