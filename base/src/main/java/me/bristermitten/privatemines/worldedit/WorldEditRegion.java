package me.bristermitten.privatemines.worldedit;

import org.bukkit.Location;
import org.bukkit.World;

public class WorldEditRegion {
    private final WorldEditVector min;
    private final WorldEditVector max;
    private final WorldEditVector center;
    private final World world;

    public WorldEditRegion(WorldEditVector min, WorldEditVector max, World world) {
        this.min = min;
        this.max = max;
        this.world = world;

        this.center = new WorldEditVector(
                (min.getX() + max.getX()) / 2,
                (min.getY() + max.getY()) / 2,
                (min.getZ() + max.getZ()) / 2);
    }

    public WorldEditVector getMinimumPoint() {
        return min;
    }

    public Location getMinimumLocation() {
        return new Location(getWorld(), min.getX(), min.getY(), min.getZ());
    }

    public WorldEditVector getMaximumPoint() {
        return max;
    }

    public Location getMaximumLocation() {
        return new Location(getWorld(), max.getX(), max.getY(), max.getZ());
    }

    @Override
    public String toString() {
        return "WorldEditRegion{" +
                "min=" + min +
                ", max=" + max +
                ", center=" + center +
                ", world=" + world +
                '}';
    }

    public boolean contains(WorldEditVector vector) {
        return vector.getX() >= min.getX() && vector.getX() <= max.getX() &&
                vector.getY() >= min.getY() && vector.getY() <= max.getY() &&
                vector.getZ() >= min.getZ() && vector.getZ() <= max.getZ();
    }

    public World getWorld() {
        return world;
    }
}
