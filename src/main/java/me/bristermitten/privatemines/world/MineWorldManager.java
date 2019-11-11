package me.bristermitten.privatemines.world;

import me.bristermitten.privatemines.config.PMConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import static me.bristermitten.privatemines.world.MineWorldManager.Direction.NORTH;

public class MineWorldManager {

    private final World minesWorld;
    private final int borderDistance;
    private int distance = 0;
    private Direction direction;

    public MineWorldManager(PMConfig config) {
        this.minesWorld = Bukkit.createWorld(
                new WorldCreator(config.getWorldName())
                        .generator(new EmptyWorldGenerator()));
        this.borderDistance = config.getMinesDistance();
        this.direction = NORTH;
    }

//    public MineWorldManager(Gangs gangs) {
//        this(gangs.cellCache);
//    }
//
//    public MineWorldManager(CellCache cellCache) {
//        this(cellCache, NORTH);
//    }
//
//    public MineWorldManager(CellCache cellCache, Direction direction) {
//        this.direction = direction;
//        this.cellCache = cellCache;
//    }

    public int getDirection() {
        return direction.ordinal();
    }

    public void setDirection(int direction) {
        this.direction = Direction.values()[direction];
    }

    public synchronized Location nextFreeLocation() {
        Location defaultLoc = new Location(minesWorld, 0, 0, 0);
        if (distance == 0) {
            distance++;
            return defaultLoc;
        }
        if (direction == null) direction = NORTH;
        Location loc = direction.add(defaultLoc, distance * borderDistance);
        direction = direction.next();
        if (direction == NORTH) distance++;
        return loc;

    }

    public void reset() {
        direction = NORTH;
        distance = 0;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public enum Direction {
        NORTH(0, -1), NORTH_EAST(1, -1),
        EAST(1, 0), SOUTH_EAST(1, 1),
        SOUTH(0, 1), SOUTH_WEST(-1, 1),
        WEST(-1, 0), NORTH_WEST(-1, -1);

        private final int xMulti, zMulti;

        Direction(int xMulti, int zMulti) {
            this.xMulti = xMulti;
            this.zMulti = zMulti;
        }

        Direction next() {
            return values()[(ordinal() + 1) % (values().length)];
        }


        Location add(Location loc, int value) {
            loc = loc.clone();
            return loc.add(value * xMulti, 0, value * zMulti);
        }
    }

}
