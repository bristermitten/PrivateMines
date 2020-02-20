//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.config;

import co.aikar.commands.MessageType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
    private String npcName = "Steve";
    private String npcSkin = "TraderNPC";
    private double taxPercentage = 5;

    private Map<MessageType, ChatColor> colors = new HashMap<>();

    public PMConfig(FileConfiguration configuration) {
        this.load(configuration);
    }

    public void load(FileConfiguration config) {
        this.worldName = config.getString("World-Name");
        this.minesDistance = config.getInt("Mine-Distance");
        if (config.contains("Schematic-Name")) {
            this.schematicName = config.getString("Schematic-Name");
        }

        ConfigurationSection blocks = config.getConfigurationSection("Blocks");
        for (String block : blocks.getKeys(false)) {
            this.blockTypes.put(BlockType.fromName(block), Material.getMaterial(blocks.getString(block)));
        }

        this.defaultBlock = Material.matchMaterial(config.getString("Default-Block"));
        this.blockOptions = config.getStringList("Block-Options")
                .stream().map(Material::matchMaterial).collect(Collectors.toList());
        this.npcName = config.getString("NPC-Name");
        this.npcSkin = config.getString("NPC-Skin");
        this.taxPercentage = config.getDouble("Tax-Percentage");

        ConfigurationSection colors = config.getConfigurationSection("Colors");
        
        for (String key : colors.getKeys(false)) {
            try {
                this.colors.put((MessageType) MessageType.class.getField(key).get(null),
                        ChatColor.valueOf(colors.getString(key)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<MessageType, ChatColor> getColors() {
        return colors;
    }

    public String getNPCSkin() {
        return npcSkin;
    }

    public double getTaxPercentage() {
        return taxPercentage;
    }

    public String getNPCName() {
        return npcName;
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
