package me.bristermitten.privatemines.worldedit;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import me.bristermitten.privatemines.data.MineSchematic;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class WE1_13MineSchematic extends MineSchematic<Clipboard> {
    protected WE1_13MineSchematic(String name, List<String> description, File file, ItemStack icon) {
        super(name, description, file, icon);
    }

    @Override
    public Clipboard getSchematic() {
        final ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (final ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
