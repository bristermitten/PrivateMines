//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.data;

import me.bristermitten.privatemines.util.Util;
import me.bristermitten.privatemines.worldedit.WorldEditRegion;
import me.bristermitten.privatemines.worldedit.WorldEditVector;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MineLocations implements ConfigurationSerializable {
    private Location spawnPoint;
    private WorldEditRegion region;
    private IWrappedRegion wgRegion;

    /*
       Gets the mine locations, e.g. Spawn point and mine region.
     */
    public MineLocations(Location spawnPoint, WorldEditVector mineAreaMin, WorldEditVector mineAreaMax, IWrappedRegion wgRegion) {
        Objects.requireNonNull(spawnPoint, "SpawnPoint");
        Objects.requireNonNull(wgRegion, "WorldGuardRegion");
        this.spawnPoint = spawnPoint;
        this.region = new WorldEditRegion(mineAreaMin, mineAreaMax, spawnPoint.getWorld());
        this.wgRegion = wgRegion;
    }

    @SuppressWarnings("unchecked")
    public static MineLocations deserialize(Map<String, Object> map) {
        Location spawnPoint = Location.deserialize((Map<String, Object>) map.get("SpawnPoint"));
        WorldEditVector min = Util.toWEVector(org.bukkit.util.Vector.deserialize((Map<String, Object>) map.get("Min")));
        WorldEditVector max = Util.toWEVector(org.bukkit.util.Vector.deserialize((Map<String, Object>) map.get("Max")));
        IWrappedRegion wgRegion = WorldGuardWrapper.getInstance().getRegion(spawnPoint.getWorld(), (String) map.get("Region"))
                .orElseThrow(() -> new IllegalArgumentException("No Region " + map.get("Region")));

        return new MineLocations(spawnPoint, min, max, wgRegion);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("SpawnPoint", this.spawnPoint.serialize());
        map.put("Min", Util.toBukkitVector(this.region.getMinimumPoint()).serialize());
        map.put("Max", Util.toBukkitVector(this.region.getMaximumPoint()).serialize());
        map.put("Region", this.wgRegion.getId());
        return map;
    }

    public Location getSpawnPoint() {
        return this.spawnPoint;
    }

    public IWrappedRegion getWgRegion() {
        return this.wgRegion;
    }

    public void setSpawnPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public WorldEditRegion getRegion() {
        return this.region;
    }
}
