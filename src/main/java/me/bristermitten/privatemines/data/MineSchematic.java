package me.bristermitten.privatemines.data;

import com.boydti.fawe.object.schematic.Schematic;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MineSchematic
{

    private final String name;
    private final List<String> description;
    private final File file;
    private final ItemStack icon;

    public MineSchematic(String name, List<String> description, File file, ItemStack icon)
    {
        this.name = name;
        this.description = description;
        this.file = file;
        this.icon = icon;
    }

    public Schematic getSchematic()
    {
        if (!file.exists())
        {
            throw new IllegalStateException("File " + file.getAbsolutePath() + "  does not exist");
        }
        ClipboardFormat format = ClipboardFormat.SCHEMATIC;
        try
        {
            return format.load(new FileInputStream(file));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MineSchematic)) return false;
        MineSchematic that = (MineSchematic) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getFile(), that.getFile()) &&
                Objects.equals(getIcon(), that.getIcon());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getName(), getDescription(), getFile(), getIcon());
    }

    public String getName()
    {
        return name;
    }

    public List<String> getDescription()
    {
        return description;
    }

    public File getFile()
    {
        return file;
    }

    public ItemStack getIcon()
    {
        return icon;
    }
}
