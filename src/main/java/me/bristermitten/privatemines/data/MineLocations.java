package me.bristermitten.privatemines.data;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.bristermitten.privatemines.Util;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class MineLocations implements ConfigurationSerializable {
    private Location spawnPoint;

    private Region region;

    public MineLocations(Location spawnPoint, Vector mineAreaMin, Vector mineAreaMax) {
        this.spawnPoint = spawnPoint;
        this.region = new CuboidRegion(mineAreaMin, mineAreaMax);
    }

    public static MineLocations deserialize(Map<String, Object> map) {
        Location spawnPoint = (Location) map.get("SpawnPoint");
        Vector min = Util.toVector((org.bukkit.util.Vector) map.get("Min"));
        Vector max = Util.toVector((org.bukkit.util.Vector) map.get("Max"));
        return new MineLocations(spawnPoint, min, max);
    }

    //TODO cleaner serialization
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("SpawnPoint", spawnPoint);
        map.put("Min", Util.toVector(region.getMinimumPoint()));
        map.put("Max", Util.toVector(region.getMaximumPoint()));
        return map;
    }


    public Location getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public Region getRegion() {
        return region;
    }
}
