package me.bristermitten.privatemines.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class ModernWEHook implements WorldEditHook<Clipboard> {

    public static Region transform(WorldEditRegion region) {
        return new CuboidRegion(
                BukkitAdapter.adapt(region.getWorld()),
                transform(region.getMinimumPoint()),
                transform(region.getMaximumPoint())
        );
    }

    public static BlockVector3 transform(WorldEditVector vector) {
        return BlockVector3.at(vector.getX(), vector.getY(), vector.getZ());
    }

    public static WorldEditVector transform(BlockVector3 vector) {
        return new WorldEditVector(vector.getX(), vector.getY(), vector.getZ());
    }


    @Override
    public void fill(WorldEditRegion region, List<ItemStack> blocks) {
        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(region.getWorld()), -1)) {
            session.setFastMode(true);

            final RandomPattern pattern = new RandomPattern();
            final Region weRegion = transform(region);

            for (ItemStack itemStack : blocks) {
                Pattern pat = (Pattern) BukkitAdapter.adapt(itemStack.getType().createBlockData());
                pattern.add(pat, 1.0);
            }
            session.setBlocks(weRegion, pattern);
            session.flushSession();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }


    @Override
    public MineFactoryCompat<Clipboard> createMineFactoryCompat() {
        return new ModernMineFactoryCompat(PrivateMines.getPlugin().getMineManager()); //Have to use static here because our compat module can't depend on the main plugin module :(
    }

    @Override
    public MineSchematic<Clipboard> loadMineSchematic(String name, List<String> description, File file, ItemStack item, Integer tier) {
        return new ModernWEMineSchematic(name, description, file, item, tier);
    }
}
