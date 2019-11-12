package me.bristermitten.privatemines;

import org.bukkit.util.Vector;

public class Util {

    public static Vector toVector(com.sk89q.worldedit.Vector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static com.sk89q.worldedit.Vector toVector(Vector v) {
        return new com.sk89q.worldedit.Vector(v.getX(), v.getY(), v.getZ());
    }
}
