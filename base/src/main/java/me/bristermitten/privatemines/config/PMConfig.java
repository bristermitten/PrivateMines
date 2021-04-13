package me.bristermitten.privatemines.config;

import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.*;
import java.util.stream.Collectors;

public class PMConfig {
    private final Map<BlockType, ItemStack> blockTypes = new EnumMap<>(BlockType.class);
    private final Map<MessageType, ChatColor> colors = new HashMap<>();
    private String worldName = "me/bristermitten/privatemines";
    private int minesDistance = 150;
    private List<ItemStack> blockOptions = new ArrayList<>();
    private List<ItemStack> mineBlocks = new ArrayList<>();

    private List<String> firstTimeCommands;
    private List<String> commands;
    private List<String> resetStyles;

    private List<ParticleEffect> effects;

    private List<Integer> resetPercentages;

    private boolean npcsEnabled = true;
    private String npcName = "Steve";
    private String npcSkin = "TraderNPC";
    private String mineRegionNameFormat = "mine-{uuid}";
    private String sellCommand = "sellall";
    private double taxPercentage = 5;
    private int resetDelay = 30;
    private int resetPercentage = 50;

    private int mineTier = 1;

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
        this.resetPercentage = config.getInt("Reset-Percentage");
        this.mineTier = config.getInt("Tier");
        this.firstTimeCommands = config.getStringList("FirstTimeCommands");
        this.commands = config.getStringList("Commands");
        this.resetStyles = config.getStringList("Reset-Styles");
        this.effects = (List<ParticleEffect>) config.getList("effects");
        this.mineRegionNameFormat = config.getString("mine-region-name");
        this.mineBlocks = (List<ItemStack>) config.getList("blocks");
        this.sellCommand = config.getString("sellCommand");
        this.resetPercentages = config.getIntegerList("Reset-Percentages");

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

    public List<ItemStack> getDefaultBlock() {
        return mineBlocks;
    }

    public List<ItemStack> getBlockOptions() {
        return blockOptions;
    }

    public List<ItemStack> getMineBlocks() {
        return mineBlocks;
    }

    public List<String> getFirstTimeCommands() { return firstTimeCommands; }
    public List<String> getCommands() {return commands;}
    public List<String> getResetStyles() {return resetStyles; }

    public List<Integer> getResetPercentages() {return resetPercentages; }

    public List<ParticleEffect> getParticleEffects() {return effects; }

    public int getResetDelay() { return resetDelay; }

    public int getResetPercentage() { return resetPercentage; }

    public int getMineTier() { return mineTier; }

    public String getMineRegionNameFormat() {return mineRegionNameFormat;}
}