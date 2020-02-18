//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.UntouchedOdin0.privatemines.config;

import co.aikar.commands.MessageType;
import org.bukkit.ChatColor;
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

        for (String block : config.getConfigurationSection("Blocks").getKeys(false)) {
            this.blockTypes.put(BlockType.fromName(block), Material.getMaterial(config.getString("Blocks." + block)));
        }

        this.defaultBlock = Material.matchMaterial(config.getString("Default-Block"));
        this.blockOptions =
                config.getStringList("Block-Options").stream().map(Material::matchMaterial).collect(Collectors.toList());
        this.npcName = config.getString("NPC-Name");
        this.npcSkin = config.getString("NPC-Skin");
        this.taxPercentage = config.getDouble("Tax-Percentage");

        for (String key : config.getConfigurationSection("Colors").getKeys(false)) {
            try {
                colors.put((MessageType) MessageType.class.getField(key).get(null),
                        ChatColor.valueOf(config.getString("Colors." + key)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<MessageType, ChatColor> getColors() {
        return colors;
    }

    public String getNpcSkin() {
        return this.npcSkin;
    }

    public double getTaxPercentage() {
        return this.taxPercentage;
    }

    public String getNPCName() {
        return npcName;
    }

    public Map<BlockType, Material> getBlockTypes() {
        return this.blockTypes;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public int getMinesDistance() {
        return this.minesDistance;
    }

    public String getSchematicName() {
        return this.schematicName;
    }

    public Material getDefaultBlock() {
        return this.defaultBlock;
    }

    public List<Material> getBlockOptions() {
        return this.blockOptions;
    }
}
