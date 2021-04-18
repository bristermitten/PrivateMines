package me.bristermitten.privatemines.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModernWEHook implements WorldEditHook {

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

    public void fillAir(WorldEditRegion region) {
        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(region.getWorld()), -1)) {
            session.setFastMode(true);
            RandomPattern pat = new RandomPattern(); // Create the random pattern

            Pattern air = (Pattern) BukkitAdapter.adapt(Material.AIR.createBlockData());

            pat.add(air, 1.0);

            final Region weRegion = transform(region);
            session.setBlocks(weRegion, pat);
            session.flushSession();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    public int getTotalBlocks(WorldEditRegion region, Set<BaseBlock> mineBlocks) {
        int totalBlocks;

        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().
                getEditSession(BukkitAdapter.adapt(region.getWorld()), -1)) {
            session.setFastMode(true);

            Vector min1 = new Vector(
                    region.getMinimumLocation().getBlockX(),
                    region.getMinimumLocation().getBlockY(),
                    region.getMinimumLocation().getBlockZ());

            Vector max1 = new Vector(
                    region.getMaximumLocation().getBlockX(),
                    region.getMaximumLocation().getBlockY(),
                    region.getMaximumLocation().getBlockZ());

            CuboidRegion regions = new CuboidRegion(BlockVector3.at(min1.getX(), min1.getY(), min1.getZ()),
                    BlockVector3.at(max1.getX(), max1.getY(), max1.getZ()));

            totalBlocks = session.countBlocks(regions, mineBlocks);
        }
        return totalBlocks;
    }

    public int getAirBlocks(WorldEditRegion region) {
        int totalBlocks;
        Set<Integer> airBlocks = new HashSet<>();
        airBlocks.add(0);

        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().
                getEditSession(BukkitAdapter.adapt(region.getWorld()), -1)) {
            session.setFastMode(true);

            Vector min1 = new Vector(
                    region.getMinimumLocation().getBlockX(),
                    region.getMinimumLocation().getBlockY(),
                    region.getMinimumLocation().getBlockZ());

            Vector max1 = new Vector(
                    region.getMaximumLocation().getBlockX(),
                    region.getMaximumLocation().getBlockY(),
                    region.getMaximumLocation().getBlockZ());

            CuboidRegion regions = new CuboidRegion(BlockVector3.at(min1.getX(), min1.getY(), min1.getZ()),
                    BlockVector3.at(max1.getX(), max1.getY(), max1.getZ()));
            totalBlocks = -1; // this bad need to fix :D
        }
        return totalBlocks;
    }

    @Override
    public MineFactoryCompat<?> createMineFactoryCompat() {
        return new ModernMineFactoryCompat(PrivateMines.getPlugin().getMineManager()); //Have to use static here because our compat module can't depend on the main plugin module :(
    }

    @Override
    public MineSchematic<?> loadMineSchematic(String name, List<String> description, File file, ItemStack item) {
        return new ModernWEMineSchematic(name, description, file, item);
    }
}