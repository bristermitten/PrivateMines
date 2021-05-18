package me.bristermitten.privatemines.worldedit;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class LegacyWEMineSchematic extends MineSchematic<Schematic> {
    protected LegacyWEMineSchematic(String name, List<String> description, File file, ItemStack icon, Integer tier, Integer resetDelay) {
        super(name, description, file, icon, tier, resetDelay);
    }

    @Override
    public Schematic getSchematic() {
        if (!file.exists()) {
            throw new IllegalStateException("File " + file.getAbsolutePath() + "  does not exist");
        }
        ClipboardFormat format = ClipboardFormat.SCHEMATIC;
        try {
            return format.load(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
