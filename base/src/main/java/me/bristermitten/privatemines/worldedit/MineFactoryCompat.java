package me.bristermitten.privatemines.worldedit;

import org.bukkit.Location;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

public interface MineFactoryCompat<S> {
    void setMineFlags(IWrappedRegion region);

    void setMainFlags(IWrappedRegion region);

    WorldEditRegion pasteSchematic(S mineSchematic, Location location);

    Iterable<WorldEditVector> loop(WorldEditRegion region);
}
