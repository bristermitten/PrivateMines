package me.bristermitten.privatemines.worldedit;

import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public interface WorldEditHook {
    void fill(WorldEditRegion region);

    MineFactoryCompat<?> createMineFactoryCompat();

    MineSchematic<?> loadMineSchematic(String name, List<String> description, File file, ItemStack item);
}
