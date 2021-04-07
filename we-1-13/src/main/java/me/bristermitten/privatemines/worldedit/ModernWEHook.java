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

    public void fill(WorldEditRegion region) {
        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(region.getWorld()), -1)) {
            session.setFastMode(true);

////            for (ItemStack block : blocks) {
////                String name = block.getType().toString().toLowerCase();
////                if (name.startsWith("legacy_")) {
////                    name = name.substring(7);
////                }
//
//                final BlockType blockType = BlockTypes.get(name);
//                if (blockType == null) {
//                    throw new IllegalArgumentException("Unknown block type " + name);
//                }

                RandomPattern pat = new RandomPattern(); // Create the random pattern
//
//                final BaseBlock baseBlock = blockType.getState(new HashMap<>()).toBaseBlock();
                Pattern stone = (Pattern) BukkitAdapter.adapt(Material.STONE.createBlockData());
                Pattern emeraldblock = (Pattern) BukkitAdapter.adapt(Material.EMERALD_BLOCK.createBlockData());

                pat.add(stone, 0.5);
                pat.add(emeraldblock, 0.5);

                final Region weRegion = transform(region);
                session.setBlocks(weRegion, pat);
                Bukkit.broadcastMessage("filled with modern");
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