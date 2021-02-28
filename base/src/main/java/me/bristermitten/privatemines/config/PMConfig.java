//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.config;

import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PMConfig {
    private final Map<BlockType, ItemStack> blockTypes = new EnumMap<>(BlockType.class);
    private final Map<MessageType, ChatColor> colors = new HashMap<>();
    private String worldName = "me/bristermitten/privatemines";
    private int minesDistance = 150;
    private ItemStack defaultBlock = new ItemStack(Material.STONE);
    private List<ItemStack> defaultBlocks = new ArrayList<>();
    private List<ItemStack> blockOptions = new ArrayList<>();
    private List<ItemStack> mineBlocks = new ArrayList<>();

    private List<String> commands;

    private boolean npcsEnabled = true;
    private String npcName = "Steve";
    private String npcSkin = "TraderNPC";
    private String mineRegionNameFormat = "mine-{uuid}";
    private double taxPercentage = 5;
    private int resetDelay = 30;

    public PMConfig(FileConfiguration configuration) {
        this.load(configuration);
    }

    /*
       Load up all the configuration files and details.
     */

    public void load(FileConfiguration config) {
        this.worldName = config.getString("World-Name");
        this.minesDistance = config.getInt("Mine-Distance");
        this.mineRegionNameFormat = config.getString("mine-region-name");

        ConfigurationSection blocks = config.getConfigurationSection("Blocks");
        for (String block : blocks.getKeys(false)) {
            final String blockType = blocks.getString(block);
            final Optional<ItemStack> value = Util.parseItem(blockType);
            if (!value.isPresent()) {
                throw new IllegalArgumentException("Unknown block type for " + block + " " + blockType);
            }

            this.blockTypes.put(BlockType.fromName(block), value.get());
        }

        final String defaultBlockName = config.getString("Default-Block");
        Optional<ItemStack> defaultBlock = Util.parseItem(defaultBlockName);
        if (!defaultBlock.isPresent()) {
            PrivateMines.getPlugin().getLogger().warning(() -> "Invalid default block " + defaultBlockName + ", stone will be used");
        } else {
            this.defaultBlock = defaultBlock.get();
        }
        this.blockOptions = config.getStringList("Block-Options")
                .stream()
                .map(Util::parseItem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        this.npcsEnabled = config.getBoolean("NPC-Enabled");
        this.npcName = config.getString("NPC-Name");
        this.npcSkin = config.getString("NPC-Skin");
        this.taxPercentage = config.getDouble("Tax-Percentage");
        this.resetDelay = config.getInt("Reset-Delay");
        this.commands = config.getStringList("Commands");
        this.mineRegionNameFormat = config.getString("mine-region-name");
        this.mineBlocks = (List<ItemStack>) config.getList("blocks");

        ConfigurationSection colorsSection = config.getConfigurationSection("Colors");

        for (String key : colorsSection.getKeys(false)) {
            try {
                this.colors.put((MessageType) MessageType.class.getField(key).get(null),
                        ChatColor.valueOf(colorsSection.getString(key)));
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

    public Map<BlockType, ItemStack> getBlockTypes() {
        return blockTypes;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getMinesDistance() {
        return minesDistance;
    }

    public boolean isNpcsEnabled() {
        return npcsEnabled;
    }

    public ItemStack getDefaultBlock() {
        return defaultBlock;
    }

    public List<ItemStack> getBlockOptions() {
        return blockOptions;
    }

    public List<ItemStack> getMineBlocks() {
        return mineBlocks;
    }

//    public List<ItemStack> getDefaultBlocks() {
//        return (List<ItemStack>) new ItemStack(Material.STONE);
//    }

    public List<String> getCommands() {return commands;}

    public int getResetDelay() { return resetDelay; }

    public String getMineRegionNameFormat() {return mineRegionNameFormat;}
}
