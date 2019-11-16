package me.bristermitten.privatemines.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PMConfig {

    private String worldName = "mines";
    private int minesDistance = 150;
    private String schematicName = "mine.schematic";
    private Map<BlockType, Material> blockTypes = new HashMap<>();
    private Material defaultBlock = Material.STONE;

    private List<Material> blockOptions = new ArrayList<>();

    public PMConfig(FileConfiguration configuration) {
        load(configuration);
    }

    public void load(FileConfiguration config) {
        this.worldName = config.getString("World-Name");
        this.minesDistance = config.getInt("Mine-Distance");
        if (config.contains("Schematic-Name")) {
            this.schematicName = config.getString("Schematic-Name");
        }
        for (String block : config.getConfigurationSection("Blocks").getKeys(false)) {
            blockTypes.put(BlockType.fromName(block), Material.getMaterial(config.getString("Blocks." + block)));
        }
        this.defaultBlock = Material.matchMaterial(config.getString("Default-Block"));
        this.blockOptions =
                config.getStringList("Block-Options").stream().map(Material::matchMaterial).collect(Collectors.toList());
    }

    public Map<BlockType, Material> getBlockTypes() {
        return blockTypes;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinesDistance() {
        return minesDistance;
    }

    public String getSchematicName() {
        return schematicName;
    }

    public Material getDefaultBlock() {
        return defaultBlock;
    }

    public List<Material> getBlockOptions() {
        return blockOptions;
    }
}
