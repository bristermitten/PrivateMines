package me.bristermitten.privatemines.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.bristermitten.privatemines.world.MineWorldManager;
import org.bukkit.Location;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Iterator;

public class WE1_13MineFactoryCompat implements MineFactoryCompat<Clipboard> {

    private final World world;

    public WE1_13MineFactoryCompat(MineWorldManager manager) {
        this.world = BukkitAdapter.adapt(manager.getMinesWorld());

    }

    @Override
    public void setMineFlags(IWrappedRegion region) {

    }

    @Override
    public void setMainFlags(IWrappedRegion region) {

    }

    @Override
    public WorldEditRegion pasteSchematic(Clipboard clipboard, Location location) {
        location.setY(clipboard.getOrigin().getBlockY());
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
            editSession.setFastMode(true);

            final BlockVector3 centerVector = BlockVector3.at(location.getX(), location.getY(), location.getZ());
            final Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(centerVector)
                    .ignoreAirBlocks(false)
                    .build();

            try {
                Operations.complete(operation);
                Region region = clipboard.getRegion();
                region.setWorld(world);
                region.shift(centerVector.subtract(clipboard.getOrigin()));

                final WorldEditVector min = WE1_13Hook.transform(region.getMinimumPoint());
                final WorldEditVector max = WE1_13Hook.transform(region.getMaximumPoint());

                return new WorldEditRegion(min, max, location.getWorld());
            } catch (WorldEditException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public Iterable<WorldEditVector> loop(WorldEditRegion region) {

        final Iterator<BlockVector3> vectors = WE1_13Hook.transform(region).iterator();
        final Iterator<WorldEditVector> weVecIterator = new Iterator<WorldEditVector>() {
            @Override
            public boolean hasNext() {
                return vectors.hasNext();
            }

            @Override
            public WorldEditVector next() {
                return WE1_13Hook.transform(vectors.next());
            }
        }; //Iterator#map when java ;-;
        return () -> weVecIterator;
    }


}
