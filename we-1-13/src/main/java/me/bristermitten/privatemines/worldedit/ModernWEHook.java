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
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

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

    public void fill(WorldEditRegion region, List<Material> blocks) {
        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(region.getWorld()), -1)) {
            session.setFastMode(true);

            final RandomPattern pattern = new RandomPattern();
            final Region weRegion = transform(region);

            Pattern stone = (Pattern) BukkitAdapter.adapt(Material.STONE.createBlockData());
            Pattern emeraldblock = (Pattern) BukkitAdapter.adapt(Material.EMERALD_BLOCK.createBlockData());
            Pattern goldblock = (Pattern) BukkitAdapter.adapt(Material.GOLD_BLOCK.createBlockData());
            Pattern diamondblock = (Pattern) BukkitAdapter.adapt(Material.DIAMOND_BLOCK.createBlockData());

            pattern.add(stone, 1.0);
            pattern.add(emeraldblock, 1.0);
            pattern.add(goldblock, 1.0);
            pattern.add(diamondblock, 1.0);


            session.setBlocks(weRegion, pattern);
            Bukkit.broadcastMessage("filled with modern");
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
            Bukkit.broadcastMessage("filled with modern (air)");
            session.flushSession();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
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