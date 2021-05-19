package me.bristermitten.privatemines.service;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class SchematicStorage {
    private static final SchematicStorage instance = new SchematicStorage();
    private final Map<String, MineSchematic<?>> schematics = new HashMap<>();
    private Map<Integer, MineSchematic> schematicTreeMap = new TreeMap<>();

    int totalSchematics = 0;

    private final File schematicsDir = new File(PrivateMines.getPlugin().getDataFolder(), "schematics");
    private MineSchematic<?> defaultSchematic = null;

    private SchematicStorage() {
    }

    public static SchematicStorage getInstance() {
        return instance;
    }

    public void setTotalSchematics(int totalSchematics) {
        this.totalSchematics = totalSchematics;
    }

    public void loadAll(final YamlConfiguration config) {
        schematics.clear();
        schematicTreeMap.clear();

        final ConfigurationSection section = config.getConfigurationSection("Schematics");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                final ConfigurationSection schematicSection = section.getConfigurationSection(name);

                List<String> description = null;
                if (schematicSection != null) {
                    description = schematicSection.getStringList("Description");
                }

                if (schematicSection != null) {
                    if (schematicSection.getString("File") == null) {
                        PrivateMines.getPlugin().getLogger().warning((() -> "Path file was null!"));
                    } else {
                        if (schematicSection.getString("File") != null) {
                            //TODO It keeps assuming this is null need to somehow fix

                            final File file = new File(schematicsDir, schematicSection.getString("File"));

                            if (!file.exists()) {
                                PrivateMines.getPlugin().getLogger().warning(() -> "Schematic " + file + " does not exist, not registered");
                                continue;
                            }

                            final Integer tier = schematicSection.getInt("Tier");
                            final Integer resetDelay = schematicSection.getInt("Reset-Delay");

                            final ItemStack item = Util.deserializeStack(schematicSection.getConfigurationSection("Icon").getValues(true));

                            final MineSchematic<?> schematic = PrivateMines.getPlugin().getWeHook().loadMineSchematic(
                                    name, description, file, item, tier, resetDelay);
                            
                            schematics.put(name, schematic);
                            schematicTreeMap.putIfAbsent(schematic.getTier(), schematic);

                            Bukkit.getLogger().info(() -> "Total schematics: " + schematicTreeMap.size());
                            setTotalSchematics(schematicTreeMap.size());
                            if (schematicSection.getBoolean("Default")) {
                                defaultSchematic = schematic;
                            }
                        }
                    }
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

    public int getTotalSchematics() {
        return totalSchematics;
    }

    public MineSchematic<?> getNextSchematic(PrivateMine privateMine) {
        MineSchematic<?> result;

        int current = privateMine.getMineTier();

        if (current + 1 > totalSchematics) {
            result = privateMine.getCurrentMineSchematic();
        } else {
            current++;
            result = schematicTreeMap.get(current);
        }
        return result;
    }
}
