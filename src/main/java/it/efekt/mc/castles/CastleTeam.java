package it.efekt.mc.castles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CastleTeam {
    private List<String> players = new ArrayList<>();
    private Location flagBlockLocation;
    private Team scoreboardTeam;

    public boolean isFlagPlaced() {
        return this.flagBlockLocation != null;
    }

    public boolean hasPlayer(Player player){
        return this.players.contains(player.getUniqueId().toString());
    }

    public boolean hasPlayerUUID(String playerUuid){
       return this.players.stream().anyMatch(item -> item.equalsIgnoreCase(playerUuid));
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

    public void updateFlagBlockLocation(Location location){
        this.flagBlockLocation = location;
    }

    public Location getFlagBlockLocation(){
        return this.flagBlockLocation;
    }


    public String getName() {
        return this.scoreboardTeam.getName();
    }

    public ChatColor getColor(){
        return this.scoreboardTeam.getColor();
    }

    public Team getScoreboardTeam() {
        return scoreboardTeam;
    }

    public void setScoreboardTeam(Team scoreboardTeam) {
        this.scoreboardTeam = scoreboardTeam;
    }
}
