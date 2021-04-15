package me.bristermitten.privatemines.world;

import me.bristermitten.privatemines.config.PMConfig;
import org.bukkit.*;

import java.util.HashMap;
import java.util.Map;

import static me.bristermitten.privatemines.world.MineWorldManager.Direction.NORTH;

public class MineWorldManager {
    private final World minesWorld;
    private final Location defaultLocation;
    private final int borderDistance;
    private int distance = 0;
    private Direction direction;

    public MineWorldManager(PMConfig config) {
        this.minesWorld = Bukkit.createWorld(
                new WorldCreator(config.getWorldName())
                        .type(WorldType.FLAT)
                        .generator(new EmptyWorldGenerator()));

        this.borderDistance = config.getMinesDistance();
        this.direction = NORTH;
        defaultLocation = new Location(minesWorld, 0, 0, 0);
    }

    public World getMinesWorld() {
        return minesWorld;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Direction", direction.ordinal());
        map.put("Distance", distance);
        return map;
    }

    public void load(Map<String, Object> map) {
        this.direction = Direction.values()[(int) map.get("Direction")];
        this.distance = (int) map.get("Distance");
    }

    public synchronized Location nextFreeLocation() {
        if (distance == 0) {
            distance++;
            return defaultLocation;
        }

        if (direction == null) direction = NORTH;
        Location loc = direction.addTo(defaultLocation, distance * borderDistance);
        direction = direction.next();
        if (direction == NORTH) distance++;
        return loc;

    }

    public enum Direction {
        NORTH(0, -1), NORTH_EAST(1, -1),
        EAST(1, 0), SOUTH_EAST(1, 1),
        SOUTH(0, 1), SOUTH_WEST(-1, 1),
        WEST(-1, 0), NORTH_WEST(-1, -1);

        private final int xMulti;
        private final int zMulti;

        Direction(int xMulti, int zMulti) {
            this.xMulti = xMulti;
            this.zMulti = zMulti;
        }

        Direction next() {
            return values()[(ordinal() + 1) % (values().length)];
        }


        Location addTo(Location loc, int value) {
            return loc.clone().add(value * xMulti, 0, value * zMulti);
        }
    }
}
