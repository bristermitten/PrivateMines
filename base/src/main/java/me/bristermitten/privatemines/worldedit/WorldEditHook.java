package me.bristermitten.privatemines.worldedit;

import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public interface WorldEditHook {
    void fill(WorldEditRegion region, List<ItemStack> blocks, boolean fastMode);
    void fillSingle(WorldEditRegion region, ItemStack block, boolean fastMode);

    void fillAir(WorldEditRegion region);

    /*
        SonarLint errors up here on both of these methods saying
        Remove usage of generic wildcard type. Any clue on a fix?
     */

    MineFactoryCompat<?> createMineFactoryCompat();

    MineSchematic<?> loadMineSchematic(String name, List<String> description, File file, ItemStack item);
}
