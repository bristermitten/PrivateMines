//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.config;

import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.util.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class PMConfig {
    private String worldName = "me/bristermitten/privatemines";
    private int minesDistance = 150;
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

    /*
       Load up all the configuration files and details.
     */

    public void load(FileConfiguration config) {
        this.worldName = config.getString("World-Name");
        this.minesDistance = config.getInt("Mine-Distance");

        ConfigurationSection blocks = config.getConfigurationSection("Blocks");
        for (String block : blocks.getKeys(false)) {
            final Optional<XMaterial> value = XMaterial.matchXMaterial(blocks.getString(block));
            if(!value.isPresent()) {
                throw new IllegalArgumentException("Unknown block type for " + block + " " + blocks.getString(block));
            }

            this.blockTypes.put(BlockType.fromName(block), value.get().parseMaterial());
        }

        this.defaultBlock = Material.matchMaterial(config.getString("Default-Block"));
        this.blockOptions = config.getStringList("Block-Options")
                .stream().map(Material::matchMaterial)
                .collect(Collectors.toList());
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


    public Material getDefaultBlock() {
        return defaultBlock;
    }

    public List<Material> getBlockOptions() {
        return blockOptions;
    }
}
