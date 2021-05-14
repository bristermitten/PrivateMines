package me.bristermitten.privatemines.worldedit;

import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;

public interface WorldEditHook<S> {
    void fill(WorldEditRegion region, List<ItemStack> blocks, boolean fastMode);

    default void fillSingle(WorldEditRegion region, ItemStack block, boolean fastMode) {
        fill(region, Collections.singletonList(block), fastMode);
    }

    default void fillAir(WorldEditRegion region, boolean fastMode) {
        fillSingle(region, new ItemStack(Material.AIR), fastMode);
    }

    /*
        SonarLint errors up here on both of these methods saying
        Remove usage of generic wildcard type. Any clue on a fix?
     */

    MineFactoryCompat<S> createMineFactoryCompat();

    MineSchematic<S> loadMineSchematic(String name, List<String> description, File file, ItemStack item, Integer tier);
}
