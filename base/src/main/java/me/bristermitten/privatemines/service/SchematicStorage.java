package me.bristermitten.privatemines.service;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicStorage {
    private static final SchematicStorage instance = new SchematicStorage();
    private final Map<String, MineSchematic<?>> schematics = new HashMap<>();
    private final File schematicsDir = new File(PrivateMines.getPlugin().getDataFolder(), "schematics");
    private MineSchematic<?> defaultSchematic = null;

    private SchematicStorage() {
    }

    public static SchematicStorage getInstance() {
        return instance;
    }

    public void loadAll(final YamlConfiguration config) {
        schematics.clear();

        final ConfigurationSection section = config.getConfigurationSection("Schematics");
        for (String name : section.getKeys(false)) {
            final ConfigurationSection schematicSection = section.getConfigurationSection(name);

            List<String> description = null;
            if (schematicSection != null) {
                description = schematicSection.getStringList("Description");
            }

            if (schematicSection.getString("File") == null) {
                PrivateMines.getPlugin().getLogger().warning((() -> "Path file was null!"));
            } else {
                final File file = new File(schematicsDir, schematicSection.getString("File"));


                if (!file.exists()) {
                    PrivateMines.getPlugin().getLogger().warning(() -> "Schematic " + file + " does not exist, not registered");
                    continue;
                }

                final ItemStack item = Util.deserializeStack(schematicSection.getConfigurationSection("Icon").getValues(true));

                final MineSchematic<?> schematic = PrivateMines.getPlugin().getWeHook().loadMineSchematic(name, description, file, item);
                schematics.put(name, schematic);

                if (schematicSection.getBoolean("Default")) {
                    defaultSchematic = schematic;
                }
            }
        }
    }

    public MineSchematic<?> get(final String name) {
        return schematics.get(name);
    }

    public Collection<MineSchematic<?>> getAll() {
        return schematics.values();
    }


    public MineSchematic<?> getDefaultSchematic() {
        return defaultSchematic;
    }
}
