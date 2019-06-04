package it.efekt.mc.castles;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CastleTeam {
    private List<String> players = new ArrayList<>();
    private String name;


    public boolean hasPlayer(Player player){
        return this.players.contains(player.getUniqueId().toString());
    }

    public boolean hasPlayerUUID(String playerUuid){
        for (String uuid : this.players){
            if (uuid.equalsIgnoreCase(playerUuid)) {
                return true;
            }
        }
        return false;
    }

    public void addPlayer(Player player){
        this.players.add(player.getUniqueId().toString());
    }

    public void removePlayer(Player player){
        if (hasPlayer(player)){
            this.players.remove(player);
        }
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (String uuid : this.players){
            players.add(Bukkit.getPlayer(UUID.fromString(uuid)));
        }
        return players;
    }

    public String getPlayersAsString(){
        String players = "";
        for (Player player : getPlayers()){
            players = players.concat(player.getName() + " ");
        }

        return players;
    }

    public void setPlayers(List<Player> players) {
        for (Player player : players){
            this.players.add(player.getUniqueId().toString());
        }
    }
}
