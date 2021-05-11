package me.bristermitten.privatemines.config;

import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PMConfig {
    private final Map<BlockType, ItemStack> blockTypes = new EnumMap<>(BlockType.class);
    private final Map<MessageType, ChatColor> colors = new HashMap<>();

    private String worldName = "privatemines";
    private int minesDistance = 150;
    private List<ItemStack> blockOptions = new ArrayList<>();
    private List<ItemStack> mineBlocks = new ArrayList<>();
    private List<ItemStack> defaultMineBlocks = new ArrayList<>();

    private List<String> firstTimeCommands;
    private List<String> commands;
    private List<String> resetStyles;
    private List<String> blockStyles;

    private List<Integer> resetPercentages;

    private boolean npcsEnabled = true;

    private String npcName = "Steve";
    private String mineRegionNameFormat = "mine-{uuid}";
    private String sellCommand = "sellall";
    private double taxPercentage = 5;
    private int resetDelay = 30;
    private int resetPercentage = 50;

    private int mineTier = 1;

    public PMConfig(FileConfiguration configuration) {
        this.load(configuration);
    }


    private void loadBlockTypes(@NotNull final ConfigurationSection configuration) {
        final ConfigurationSection blocksSection = configuration.getConfigurationSection("Blocks");
        if (blocksSection != null) {
            for (String block : blocksSection.getKeys(false)) {
                final ItemStack blockValue = Optional.ofNullable(blocksSection.getString(block))
                        .flatMap(Util::parseItem) //Monad pattern in java :)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown block type for " + block + " " + blocksSection.getString(block)));
                this.blockTypes.put(BlockType.fromName(block), blockValue);
            }
        }
    }

    public void load(@NotNull final ConfigurationSection config) {
        this.worldName = config.getString("World-Name");
        this.minesDistance = config.getInt("Mine-Distance");
        this.mineRegionNameFormat = config.getString("mine-region-name");
        this.npcsEnabled = config.getBoolean("NPC-Enabled");
        this.npcName = config.getString("NPC-Name");
        this.sellCommand = config.getString("Sell-Command");
        this.taxPercentage = config.getDouble("Tax-Percentage");
        this.resetDelay = config.getInt("Reset-Delay");
        this.resetPercentage = config.getInt("Reset-Percentage");
        this.mineTier = config.getInt("Tier");
        this.firstTimeCommands = config.getStringList("FirstTimeCommands");
        this.commands = config.getStringList("Commands");
        this.resetStyles = config.getStringList("Reset-Styles");
        this.mineRegionNameFormat = config.getString("mine-region-name");

        loadBlockTypes(config);

        this.blockOptions = config.getStringList("Block-Options")
                .stream()
                .map(Util::parseItem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        this.blockStyles = config.getStringList("Block-Styles");

        this.defaultMineBlocks = config.getStringList("Block-Styles.Default")
                .stream()
                .map(Util::parseItem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        this.mineBlocks = config.getStringList("blocks")
                .stream()
                .map(Util::parseItem)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        this.resetPercentages = config.getIntegerList("Reset-Percentages");

        ConfigurationSection colorsSection = config.getConfigurationSection("Colors");

        if (colorsSection != null) {
            for (String key : colorsSection.getKeys(false)) {
                try {
                    this.colors.put(
                            (MessageType) MessageType.class.getField(key).get(null),
                            ChatColor.valueOf(colorsSection.getString(key)));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Map<MessageType, ChatColor> getColors() {
        return colors;
    }


    public String getNPCName() {
        return npcName;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getMineRegionNameFormat() {
        return mineRegionNameFormat;
    }

    public String getSellCommand() {
        return sellCommand;
    }

    public double getTaxPercentage() {
        return taxPercentage;
    }

    public Map<BlockType, ItemStack> getBlockTypes() {
        return blockTypes;
    }

    public int getMinesDistance() {
        return minesDistance;
    }

    public boolean isNpcsEnabled() {
        return npcsEnabled;
    }

    public List<ItemStack> getBlockOptions() {
        return blockOptions;
    }

    public List<ItemStack> getDefaultMineBlocks() {
        return defaultMineBlocks;
    }

    public List<ItemStack> getMineBlocks() {
        return mineBlocks;
    }

    public List<String> getFirstTimeCommands() {
        return firstTimeCommands;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getResetStyles() {
        return resetStyles;
    }

    public List<String> getBlockStyles() {
        return blockStyles;
    }

    public List<Integer> getResetPercentages() {
        return resetPercentages;
    }

    public int getResetDelay() {
        return resetDelay;
    }

    public int getResetPercentage() {
        return resetPercentage;
    }

    public int getMineTier() {
        return mineTier;
    }

}
