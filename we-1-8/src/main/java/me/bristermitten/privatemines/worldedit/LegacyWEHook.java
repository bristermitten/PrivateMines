package me.bristermitten.privatemines.worldedit;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class LegacyWEHook implements WorldEditHook<Schematic> {

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

    // please stop making me suffer by working with such a old version.... Just update to 1.13 or higher.

    /**
     * @deprecated - Please just update to 1.13 for fucks sake.
     * @param region - The WorldEdit region to set the blocks in
     * @param blocks - The blocks to fill the mine with.
     */

    @Deprecated
    public void fill(WorldEditRegion region, List<ItemStack> blocks) {
        final EditSession session = new EditSessionBuilder(FaweAPI.getWorld(region.getWorld().getName()))
                .fastmode(true)
                .build();

        final RandomPattern pattern = new RandomPattern();

        for (ItemStack is : blocks) {
            //noinspection deprecation
            Pattern pat = new BaseBlock(is.getTypeId());
            pattern.add(pat, 1);
        }

        session.setBlocks(transform(region), pattern);
        session.flushQueue();
    }

    @Override
    public MineFactoryCompat<Schematic> createMineFactoryCompat() {
        return new LegacyWEMineFactoryCompat(PrivateMines.getPlugin().getMineManager()); //Have to use static here because our compat module can't depend on the main plugin module :(
    }

    @Override
    public MineSchematic<Schematic> loadMineSchematic(String name, List<String> description, File file, ItemStack item, Integer tier, Integer resetDelay) {
        return new LegacyWEMineSchematic(name, description, file, item, tier, resetDelay);
    }
}