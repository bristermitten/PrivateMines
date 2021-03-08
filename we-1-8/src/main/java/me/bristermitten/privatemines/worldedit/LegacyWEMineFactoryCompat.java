package me.bristermitten.privatemines.worldedit;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.boydti.fawe.object.visitor.FastIterator;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.World;
import me.bristermitten.privatemines.world.MineWorldManager;
import org.bukkit.Location;

import java.util.Iterator;

public class LegacyWEMineFactoryCompat implements MineFactoryCompat<Schematic> {

    private final EditSession editSession;
    private final World world;

    public LegacyWEMineFactoryCompat(MineWorldManager manager) {
        this.world = FaweAPI.getWorld(manager.getMinesWorld().getName());

        this.editSession = new EditSessionBuilder(world).allowedRegionsEverywhere().limitUnlimited()
                .fastmode(true).build();
    }

    @Override
    public WorldEditRegion pasteSchematic(Schematic schematic, Location location) {

        final Clipboard clipboard = schematic.getClipboard();

        if (clipboard == null) {
            throw new IllegalStateException("Schematic does not have a Clipboard! This should never happen!");
        }

        location.setY(clipboard.getOrigin().getBlockY());

        final Vector centerVector = BukkitUtil.toVector(location);
        schematic.paste(editSession, centerVector, false, true, null);

        Region region = clipboard.getRegion();
        region.setWorld(world);

        try {
            region.shift(centerVector.subtract(clipboard.getOrigin()));
        } catch (RegionOperationException e) {
            e.printStackTrace();
        }

        final WorldEditVector min = LegacyWEHook.transform(region.getMinimumPoint());
        final WorldEditVector max = LegacyWEHook.transform(region.getMaximumPoint());

        return new WorldEditRegion(min, max, location.getWorld());
    }

    @Override
    public Iterable<WorldEditVector> loop(WorldEditRegion region) {
        final FastIterator vectors = new FastIterator(LegacyWEHook.transform(region), editSession);
        final Iterator<Vector> fastIterator = vectors.iterator();
        final Iterator<WorldEditVector> weVecIterator = new Iterator<WorldEditVector>() {
            @Override
            public boolean hasNext() {
                return fastIterator.hasNext();
            }

            @Override
            public WorldEditVector next() {
                return LegacyWEHook.transform(fastIterator.next());
            }
        }; //Iterator#map when java ;-;
        return () -> weVecIterator;
    }


}
