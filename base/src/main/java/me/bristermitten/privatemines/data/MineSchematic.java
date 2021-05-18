package me.bristermitten.privatemines.data;

import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;
import java.util.Objects;

public abstract class MineSchematic<S> {

    private final String name;
    private final List<String> description;
    protected final File file;
    private final ItemStack icon;
    private final Integer tier;

    protected MineSchematic(String name, List<String> description, File file, ItemStack icon, Integer tier, Integer resetDelay) {
        this.name = name;
        this.description = description;
        this.file = file;
        this.icon = icon;
        this.tier = tier;
    }

    public abstract S getSchematic();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MineSchematic)) return false;
        MineSchematic<?> that = (MineSchematic<?>) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getFile(), that.getFile()) &&
                Objects.equals(getIcon(), that.getIcon()) &&
                Objects.equals(getTier(), that.getTier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getFile(), getIcon());
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }

    public File getFile() {
        return file;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public Integer getTier() { return tier; }
}
