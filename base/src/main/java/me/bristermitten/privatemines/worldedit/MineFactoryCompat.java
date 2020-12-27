package me.bristermitten.privatemines.worldedit;

import org.bukkit.Location;

public interface MineFactoryCompat<S> {

    WorldEditRegion pasteSchematic(S mineSchematic, Location location);

    Iterable<WorldEditVector> loop(WorldEditRegion region);
}
