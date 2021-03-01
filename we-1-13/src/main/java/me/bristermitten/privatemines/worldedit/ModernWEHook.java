package me.bristermitten.privatemines.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
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

    public void fill(WorldEditRegion region, ItemStack blocks) {
        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(region.getWorld()), -1)) {
            session.setFastMode(true);

            String name = blocks.getType().toString().toLowerCase();
            if (name.startsWith("legacy_")) {
                name = name.substring(7);
            }

            final BlockType blockType = BlockTypes.get(name);
            if (blockType == null) {
                throw new IllegalArgumentException("Unknown block type " + name);
            }
            final BaseBlock baseBlock = blockType.getState(new HashMap<>()).toBaseBlock();

            final Region weRegion = transform(region);
            session.setBlocks(weRegion, baseBlock);
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