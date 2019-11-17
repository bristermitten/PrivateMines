//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.data;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.bristermitten.privatemines.Util;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class MineLocations implements ConfigurationSerializable {
    private Location spawnPoint;
    private Region region;
    private ProtectedRegion wgRegion;
    private Location npcLocation;

    public MineLocations(Location spawnPoint, Vector mineAreaMin, Vector mineAreaMax, ProtectedRegion wgRegion) {
        this.spawnPoint = spawnPoint;
        this.region = new CuboidRegion(mineAreaMin, mineAreaMax);
        this.wgRegion = wgRegion;
    }

    @SuppressWarnings("unchecked")
    public static MineLocations deserialize(Map<String, Object> map) {
        Location spawnPoint = Location.deserialize((Map<String, Object>) map.get("SpawnPoint"));
        Vector min = Util.toVector(org.bukkit.util.Vector.deserialize((Map<String, Object>) map.get("Min")));
        Vector max = Util.toVector(org.bukkit.util.Vector.deserialize((Map<String, Object>) map.get("Max")));
        RegionManager regionManager = WorldGuardPlugin.inst().getRegionManager(spawnPoint.getWorld());
        ProtectedRegion wgRegion = regionManager.getRegion((String) map.get("Region"));
        return new MineLocations(spawnPoint, min, max, wgRegion);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("SpawnPoint", this.spawnPoint.serialize());
        map.put("Min", Util.toVector(this.region.getMinimumPoint()).serialize());
        map.put("Max", Util.toVector(this.region.getMaximumPoint()).serialize());
        map.put("Region", this.wgRegion.getId());
        return map;
    }

    public Location getSpawnPoint() {
        return this.spawnPoint;
    }

    public ProtectedRegion getWgRegion() {
        return this.wgRegion;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public Region getRegion() {
        return this.region;
    }
}
