package me.bristermitten.privatemines.data;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.service.SchematicStorage;
import me.bristermitten.privatemines.util.Util;
import me.bristermitten.privatemines.worldedit.WorldEditRegion;
import me.bristermitten.privatemines.worldedit.WorldEditVector;
import me.clip.autosell.SellHandler;
import me.clip.autosell.objects.Shop;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PrivateMine implements ConfigurationSerializable {

    public static final String PLAYER_PLACEHOLDER = "{player}";
    //How far between a mine reset in milliseconds
    private final int RESET_THRESHOLD;
    private final UUID owner;
    private final Set<UUID> bannedPlayers;
    private final Set<UUID> trustedPlayers;
    private final List<String> commands;
    private WorldEditRegion mainRegion;
    private MineLocations locations;
    private IWrappedRegion wgRegion;
    private UUID npcId;
    private boolean open;
    private List<ItemStack> blocks;
    private double taxPercentage;
    private double minePercentage;
    private MineSchematic<?> mineSchematic;
    private long nextResetTime;
    private final MineStorage mineStorage;
    private int mineTier;
    private String resetStyle;
    private Shop shop;

    public PrivateMine(UUID owner,
                       Set<UUID> bannedPlayers,
                       Set<UUID> trustedPlayers,
                       List<String> commands,
                       boolean open,
                       List<ItemStack> blocks,
                       WorldEditRegion mainRegion,
                       MineLocations locations,
                       IWrappedRegion wgRegion,
                       UUID npc,
                       double taxPercentage,
                       int resetTime,
                       int mineTier,
                       MineSchematic<?> mineSchematic,
                       MineStorage storage) {
        this.owner = owner;
        this.bannedPlayers = bannedPlayers;
        this.trustedPlayers = trustedPlayers;
        this.commands = commands;
        this.open = open;
        this.mainRegion = mainRegion;
        this.locations = locations;
        this.blocks = blocks;
        this.wgRegion = wgRegion;
        this.npcId = npc;
        this.taxPercentage = taxPercentage;
        this.mineSchematic = mineSchematic;
        this.RESET_THRESHOLD = (int) TimeUnit.MINUTES.toMillis(resetTime);
        this.mineTier = mineTier + 1;
        this.mineStorage = storage;
        this.shop = shop;
    }

    @SuppressWarnings("unchecked")
    public static PrivateMine deserialize(Map<String, Object> map) {

        UUID owner = UUID.fromString((String) map.get("Owner"));

        boolean open = (Boolean) map.get("Open");

        ItemStack block = (ItemStack) map.get("blocks");

        List<ItemStack> blocks = new ArrayList<>();

//        try {
//            blocks = ItemStack.deserialize((Map)map.get("Blocks"));
//        } catch (Exception ignored) {
//            blocks = ItemStack.deserialize((Map<String, Object>) map.get("Blocks"));
//        }

        WorldEditVector corner1 = Util.deserializeWorldEditVector((Map<String, Object>) map.get("Corner1"));
        WorldEditVector corner2 = Util.deserializeWorldEditVector(((Map<String, Object>) map.get("Corner2")));

        MineLocations locations = MineLocations.deserialize((Map<String, Object>) map.get("Locations"));
        WorldEditRegion mainRegion = new WorldEditRegion(corner1, corner2, locations.getSpawnPoint().getWorld());

        IWrappedRegion wgRegion = WorldGuardWrapper.getInstance().getRegion(locations.getSpawnPoint().getWorld(), owner.toString())
                .orElseThrow(() -> new IllegalStateException("Could not deserialize PrivateMine - mining region did not exist"));

        UUID npcId = UUID.fromString((String) map.get("NPC"));
        double taxPercentage = (Double) map.get("Tax");
        int resetTime = (Integer) map.get("Reset-Delay");
        int mineTier = (Integer) map.get("Tier");

        String schematicName = (String) map.get("Schematic");

        List<String> commands = (List<String>) map.get("Commands");

        MineSchematic<?> schematic = SchematicStorage.getInstance().get(schematicName);
        MineStorage storage = PrivateMines.getPlugin().getStorage();

        if (schematic == null) {
            throw new IllegalArgumentException("Invalid Schematic " + schematicName);
        }

        Set<UUID> bannedPlayers = Optional.ofNullable((List<String>) map.get("BannedPlayers"))
                .map(bans -> bans.stream().map(UUID::fromString).collect(Collectors.toSet()))
                .orElse(new HashSet<>());

        Set<UUID> trustedPlayers = Optional.ofNullable((List<String>) map.get("TrustedPlayers"))
                .map(trusted -> trusted.stream().map(UUID::fromString).collect(Collectors.toSet())).orElse(new HashSet<>());

        String shopName = "shop-" + owner.toString();
        Shop shop = new Shop(shopName);
        SellHandler.addShop(shop);

        return new PrivateMine(owner, bannedPlayers, trustedPlayers, commands, open, blocks, mainRegion, locations,
                                wgRegion, npcId, taxPercentage, resetTime, mineTier, schematic, storage);
    }

    public double getTaxPercentage() {
        return this.taxPercentage;
    }

    public double getMinePercentage() {
        return this.minePercentage;
    }

    public List<ItemStack> getMineBlocks() {
        return blocks;
    }

    public void setTaxPercentage(double amount) {
        this.taxPercentage = amount;
    }

    public void setMineTier(int tier) {this.mineTier = tier; }

    public int getMineTier() {return this.mineTier; }

    public int getResetTime() {
        return this.RESET_THRESHOLD;
    }

    public boolean contains(Player p) {
        return this.mainRegion.contains(Util.toWEVector(p.getLocation().toVector()));
    }

    public void setResetStyle(String style) {
        this.resetStyle = style;
    }

    public String getResetStyle() {
        return resetStyle;
    }

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();
        map.put("Owner", this.owner.toString());
        map.put("Open", this.open);
        map.put("Block", this.blocks);
        map.put("Locations", this.locations.serialize());
        map.put("Corner1", Util.toBukkitVector(this.mainRegion.getMinimumPoint()).serialize());
        map.put("Corner2", Util.toBukkitVector(this.mainRegion.getMaximumPoint()).serialize());
        map.put("NPC", this.npcId.toString());
        map.put("Tax", this.taxPercentage);
        map.put("Reset-Delay", this.RESET_THRESHOLD);
        map.put("Reset-Style", this.resetStyle);
        map.put("Mine-Tier", this.mineTier);
        map.put("Commands", this.commands);
        map.put("Schematic", this.mineSchematic.getName());
        map.put("BannedPlayers", this.bannedPlayers.stream().map(UUID::toString).collect(Collectors.toList()));
        map.put("TrustedPlayers", this.trustedPlayers.stream().map(UUID::toString).collect(Collectors.toList()));
        return map;
    }

    public void teleport(Player player) {
        if (bannedPlayers.contains(player.getUniqueId())) {
            BukkitCommandManager manager = PrivateMines.getPlugin().getManager();
            BukkitCommandIssuer issuer = manager.getCommandIssuer(player);
            manager.sendMessage(issuer, MessageType.ERROR, LangKeys.ERR_YOU_WERE_BANNED, PLAYER_PLACEHOLDER, Bukkit.getOfflinePlayer(owner).getName());
            return;
        }

        player.teleport(locations.getSpawnPoint());
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
    }

    public UUID getOwner() {
        return owner;
    }

    public void teleport() {
        Player player = Bukkit.getPlayer(this.owner);
        if (player != null) teleport(player);
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void fillWE() {

        final ICuboidSelection selection = (ICuboidSelection) locations.getWgRegion().getSelection();
        final WorldEditRegion miningRegion = new WorldEditRegion(
                Util.toWEVector(selection.getMinimumPoint()),
                Util.toWEVector(selection.getMaximumPoint()),
                mainRegion.getWorld()
        );
        //Could probably cache this but it's not very intensive
        //free any players who might be in the mine
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (miningRegion.contains(Util.toWEVector(player.getLocation()))) {
                player.teleport(locations.getSpawnPoint());
                player.sendMessage(ChatColor.GREEN + "You've been teleported to the mine spawn point!");
            }
        }

        PrivateMines.getPlugin().getWeHook().fill(miningRegion);

        this.nextResetTime = System.currentTimeMillis() + RESET_THRESHOLD;
    }

    private void setBlock(Block block, MaterialData blockdata) {
        block.setType(blockdata.getItemType());
    }

    private Block getBlock(World world, int x, int y, int z) {
        return (new Location(world, x, y, z)).getBlock();
    }

    public boolean shouldReset() {
        return this.locations.getSpawnPoint().getChunk().isLoaded() && System.currentTimeMillis() >= nextResetTime;
    }

    public double getMinePercents(Player player) {
        return mineStorage.get(player).getTaxPercentage();
    }

    /*
      Delete the mine.
     */
    public void delete() {

        final ICuboidSelection selection = (ICuboidSelection) locations.getWgRegion().getSelection();
        final WorldEditRegion miningRegion = new WorldEditRegion(
                Util.toWEVector(selection.getMinimumPoint()),
                Util.toWEVector(selection.getMaximumPoint()),
                mainRegion.getWorld()
        );

        /*
            Removes mine blocks + shell also
         */
        PrivateMines.getPlugin().getWeHook().fillAir(mainRegion);
        PrivateMines.getPlugin().getWeHook().fillAir(miningRegion);
        removeAllPlayers();
        removeRegion();

        if (PrivateMines.getPlugin().isNpcsEnabled()) {
            NPC npc = CitizensAPI.getNPCRegistry().getByUniqueId(npcId);
            if (npc != null) {
                npc.destroy();
            }
        }
    }

    /*
    Delete the Private Mine Region.
     */
    private void removeRegion() {
        World world = locations.getSpawnPoint().getWorld();
        WorldGuardWrapper.getInstance().removeRegion(world, wgRegion.getId());
        WorldGuardWrapper.getInstance().removeRegion(world, locations.getWgRegion().getId());
    }


    /*
      Teleport all the players back to spawn.
     */
    private void removeAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.contains(player)) {
                player.performCommand("spawn");
            }
        }
    }

    /*
      Sets the new mine schematic (Used when changing themes)
     */
    public void setMineSchematic(MineSchematic<?> mineSchematic) {
        fillWE();
//        fillWE(new ItemStack(Material.AIR));
        boolean mineIsOpen = isOpen();
        setOpen(false);
        delete();

        PrivateMine newMine = PrivateMines.getPlugin().getFactory().create(
                Bukkit.getPlayer(owner),
                mineSchematic,
                Util.toLocation(mainRegion.getCenter(), locations.getSpawnPoint().getWorld()), false);

        this.locations = newMine.locations;
        this.mainRegion = newMine.mainRegion;
        this.wgRegion = newMine.wgRegion;
        this.npcId = newMine.npcId;
        this.mineSchematic = mineSchematic;
        setOpen(mineIsOpen);
    }

    public boolean ban(Player player) {
        if (this.contains(player)) {
            BukkitCommandManager manager = PrivateMines.getPlugin().getManager();
            BukkitCommandIssuer issuer = manager.getCommandIssuer(player);
            manager.sendMessage(issuer, MessageType.ERROR, LangKeys.ERR_YOU_WERE_BANNED, PLAYER_PLACEHOLDER, Bukkit.getOfflinePlayer(owner).getName());

            player.performCommand("spawn");
        }

        return bannedPlayers.add(player.getUniqueId());
    }

    public boolean unban(Player player) {
        BukkitCommandManager manager = PrivateMines.getPlugin().getManager();
        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);
        manager.sendMessage(issuer, MessageType.ERROR, LangKeys.ERR_YOU_WERE_UNBANNED, PLAYER_PLACEHOLDER, Bukkit.getOfflinePlayer(owner).getName());

        return bannedPlayers.remove(player.getUniqueId());
    }
}