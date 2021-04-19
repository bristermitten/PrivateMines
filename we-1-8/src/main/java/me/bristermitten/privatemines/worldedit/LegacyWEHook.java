package me.bristermitten.privatemines.worldedit;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LegacyWEHook implements WorldEditHook {

    public static Region transform(WorldEditRegion region) {
        return new CuboidRegion(
                FaweAPI.getWorld(region.getWorld().getName()),
                transform(region.getMinimumPoint()),
                transform(region.getMaximumPoint())
        );
    }

    public static Vector transform(WorldEditVector vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static WorldEditVector transform(Vector vector) {
        return new WorldEditVector(vector.getX(), vector.getY(), vector.getZ());
    }

    /*
        Fill mine with one type of block
     */

    public void fillSingle(WorldEditRegion region, ItemStack block, boolean fastMode) {
        final EditSession session = new EditSessionBuilder(FaweAPI.getWorld(region.getWorld().getName()))
                .fastmode(fastMode)
                .build();

        //noinspection deprecation
        session.setBlocks(transform(region), new BaseBlock(block.getTypeId()));
        session.flushQueue();
    }

    /*
        Fill the mine with multiple blocks
     */

    public void fill(WorldEditRegion region, List<ItemStack> blocks, boolean fastMode) {
        final EditSession session = new EditSessionBuilder(FaweAPI.getWorld(region.getWorld().getName()))
                .fastmode(fastMode)
                .build();

        /*
            Please help me rework this... I'm gonna destroy something soon.
         */

        List<BlockChance> blockChance = new ArrayList<>();

        RandomFillPattern randomFillPattern = new RandomFillPattern(blockChance);

        //noinspection deprecation
        session.setBlocks(transform(region), randomFillPattern);
        session.flushQueue();
    }

    public void fillAir(WorldEditRegion region) {
        final EditSession session = new EditSessionBuilder(FaweAPI.getWorld(region.getWorld().getName()))
                .fastmode(true)
                .build();

        List<BlockChance> blockChance = new ArrayList<>();

        BlockChance air = new BlockChance(new BaseBlock(0), 100);
        blockChance.add(air);
        RandomFillPattern randomFillPattern = new RandomFillPattern(blockChance);

        //noinspection deprecation
        session.setBlocks(transform(region), randomFillPattern);
        session.flushQueue();
    }

    @Override
    public MineFactoryCompat<?> createMineFactoryCompat() {
        return new LegacyWEMineFactoryCompat(PrivateMines.getPlugin().getMineManager()); //Have to use static here because our compat module can't depend on the main plugin module :(
    }

    @Override
    public MineSchematic<?> loadMineSchematic(String name, List<String> description, File file, ItemStack item) {
        return new LegacyWEMineSchematic(name, description, file, item);
    }
}
