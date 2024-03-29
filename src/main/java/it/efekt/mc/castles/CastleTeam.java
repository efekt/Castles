package it.efekt.mc.castles;

import de.tr7zw.itemnbtapi.NBTItem;
import it.efekt.mc.castles.events.FlagReturnedToOriginEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CastleTeam {
    private List<String> players = new ArrayList<>();
    private Location flagBlockLocation;
    private Location flagBlockOriginLocation;
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
        if (location != null){
            this.flagBlockOriginLocation = location.clone();
            Bukkit.getLogger().info("Team: " + getColor().name() + " moved flag to: " + getFlagBlockOriginLocation().toString());
        }
    }

    public Location getFlagBlockLocation(){
        return this.flagBlockLocation;
    }

    public Location getFlagBlockOriginLocation() {
        return flagBlockOriginLocation;
    }

    public void setFlagBlockOriginLocation(Location flagBlockOriginLocation) {
        this.flagBlockOriginLocation = flagBlockOriginLocation.clone();
    }

    public void updateCompass(){
        for (Player player : getPlayers()){
            if (getFlagBlockLocation() != null){
                player.setCompassTarget(getFlagBlockLocation());
                return;
            }

            for (CastleTeam castleTeam : CastlesPlugin.castlesManager.getInstance().getTeams()){
                if (castleTeam.equals(this)){
                    continue;
                }

                for (Player teamPlayer : castleTeam.getPlayers()){
                    for (ItemStack itemStack : teamPlayer.getInventory().getContents()){
                        if (itemStack != null && itemStack.getType().equals(Castles.FLAG)){
                            if (new NBTItem(itemStack).hasKey(Castles.FLAG_COLOR_NBT_STRING)){
                                player.setCompassTarget(teamPlayer.getLocation());
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    // moves flag to it's original location and removes it from player's inventory
    public void moveFlagToOrigin(){
        getFlagBlockOriginLocation().getBlock().setType(Castles.FLAG);
        updateFlagBlockLocation(getFlagBlockOriginLocation());
        Bukkit.getServer().getPluginManager().callEvent(new FlagReturnedToOriginEvent(this));
    }

    public String getName() {
        return this.scoreboardTeam.getName();
    }

    public ChatColor getColor(){
        return this.scoreboardTeam.getColor();
    }

    public void setScoreboardTeam(Team scoreboardTeam) {
        this.scoreboardTeam = scoreboardTeam;
    }
}
