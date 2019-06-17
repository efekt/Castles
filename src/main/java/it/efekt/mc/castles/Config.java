package it.efekt.mc.castles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private FileConfiguration conf;
    private CastlesPlugin plugin;

    public Config(CastlesPlugin plugin){
        this.plugin = plugin;
        this.conf = this.plugin.getConfig();
    }

    public Location getSpawnLocation(){
        double spawnX = this.conf.getDouble("spawn.x");
        double spawnY = this.conf.getDouble("spawn.y");
        double spawnZ = this.conf.getDouble("spawn.z");
        double spawnYaw = this.conf.getDouble("spawn.yaw");
        String spawnWorld = this.conf.getString("spawn.world");

        return new Location(Bukkit.getWorld(spawnWorld), spawnX, spawnY, spawnZ, (float) spawnYaw, 0f);
    }

    public long getPeaceTime(){
        return this.conf.getLong("peaceLength");
    }

    public long getWarTime(){
        return this.conf.getLong("warLength");
    }

    public int getTeamCount(){
        return this.conf.getInt("teamCount");
    }

    public int getFlagNoPvpZoneRadius(){
        return this.conf.getInt("flagPvpZoneRadius");
    }

    public void reload(){
        this.plugin.reloadConfig();
    }
}
