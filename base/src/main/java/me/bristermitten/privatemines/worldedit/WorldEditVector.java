package me.bristermitten.privatemines.worldedit;

public class WorldEditVector {
    private final double x, y, z;

    public WorldEditVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public WorldEditVector copy() {
        return new WorldEditVector(x, y, z);
    }

    @Override
    public String toString() {
        return "WorldEditVector{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
