package me.bristermitten.privatemines.data;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
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
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
    private final MineStorage mineStorage;
    private WorldEditRegion mainRegion;
    private MineLocations locations;
    private IWrappedRegion wgRegion;
    private UUID npcId;
    private boolean open;
    private boolean fastMode;
    private List<ItemStack> blocks;
    private double taxPercentage;
    private double minePercentage;
    private double resetPercentage;
    private MineSchematic<?> mineSchematic;
    private long nextResetTime;
    private int mineTier;
    private String resetStyle;

    public PrivateMine(UUID owner,
                       Set<UUID> bannedPlayers,
                       Set<UUID> trustedPlayers,
                       List<String> commands,
                       boolean open,
                       boolean fastMode,
                       List<ItemStack> blocks,
                       WorldEditRegion mainRegion,
                       MineLocations locations,
                       IWrappedRegion wgRegion,
                       UUID npc,
                       double taxPercentage,
                       double resetPercent,
                       int resetTime,
                       int mineTier,
                       MineSchematic<?> mineSchematic,
                       MineStorage storage) {
        this.owner = owner;
        this.bannedPlayers = bannedPlayers;
        this.trustedPlayers = trustedPlayers;
        this.commands = commands;
        this.open = open;
        this.fastMode = fastMode;
        this.mainRegion = mainRegion;
        this.locations = locations;
        this.blocks = blocks;
        this.wgRegion = wgRegion;
        this.npcId = npc;
        this.taxPercentage = taxPercentage;
        this.resetPercentage = resetPercent;
        this.mineSchematic = mineSchematic;
        this.RESET_THRESHOLD = (int) TimeUnit.MINUTES.toMillis(resetTime);
        this.mineTier = mineTier + 1;
        this.mineStorage = storage;
    }

    @SuppressWarnings("unchecked")
    public static PrivateMine deserialize(Map<String, Object> map) {

        UUID owner = UUID.fromString((String) map.get("Owner"));

        boolean open = (Boolean) map.get("Open");
        boolean fastMode = (Boolean) map.get("FastMode");

        List<ItemStack> blocks = new ArrayList<>();

        WorldEditVector corner1 = Util.deserializeWorldEditVector((Map<String, Object>) map.get("Corner1"));
        WorldEditVector corner2 = Util.deserializeWorldEditVector(((Map<String, Object>) map.get("Corner2")));

        MineLocations locations = MineLocations.deserialize((Map<String, Object>) map.get("Locations"));
        WorldEditRegion mainRegion = new WorldEditRegion(corner1, corner2, locations.getSpawnPoint().getWorld());

        IWrappedRegion wgRegion = WorldGuardWrapper.getInstance().getRegion(locations.getSpawnPoint().getWorld(), owner.toString())
                .orElseThrow(() -> new IllegalStateException("Could not deserialize PrivateMine - mining region did not exist"));

        UUID npcId = UUID.fromString((String) map.get("NPC"));
        double taxPercentage = (Double) map.get("Tax");
        double resetPercentage = (Double) map.get("Reset-Percentage");
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

        return new PrivateMine(owner, bannedPlayers, trustedPlayers, commands, open, fastMode, blocks, mainRegion, locations,
                wgRegion, npcId, taxPercentage, resetPercentage, resetTime, mineTier, schematic, storage);
    }

    public double getTaxPercentage() {
        return this.taxPercentage;
    }

    public void setTaxPercentage(double amount) {
        this.taxPercentage = amount;
    }

    public double getMinePercentage() {
        return this.minePercentage;
    }

    public double getResetPercentage() {
        return resetPercentage;
    }

    public void setResetPercentage(double amount) {
        this.resetPercentage = amount;
    }

    public void increaseResetPercentage(double amount) {
        if (this.resetPercentage + amount > 100) {
            this.resetPercentage = 100;
        } else {
            this.resetPercentage = this.resetPercentage + amount;
        }
    }

    public void decreaseResetPercentage(double amount) {
        if (this.resetPercentage - amount < 1) {
            this.resetPercentage = 1;
        } else {
            this.resetPercentage = this.resetPercentage - amount;
        }
    }

    public List<ItemStack> getMineBlocks() {
        return blocks;
    }

    public List<String> getMineBlocksString() {
        ArrayList<String> blocks = new ArrayList<>();
        for (ItemStack itemStack : getMineBlocks()) {
            blocks.add(itemStack.getType().name());
        }
        return blocks;
    }

    public List<String> getMineBlocksFormatted(List<ItemStack> stack) {
        ArrayList<String> pretty = new ArrayList();
        for (ItemStack itemStack : stack) {
            pretty.add(Util.prettify(itemStack.getType().toString()));
        }
        return pretty;
    }

    public void setMineBlocks(List<ItemStack> itemStack) {
        this.blocks = itemStack;
    }

    public void addMineBlock(ItemStack itemStack) {
        List<ItemStack> blocksOriginal = blocks;
        blocksOriginal.add(itemStack);
        this.blocks = blocksOriginal;
    }

    public void removeMineBlock(ItemStack itemStack) {
        List<ItemStack> blocksOriginal = blocks;
        blocksOriginal.remove(itemStack);
        this.blocks = blocksOriginal;
    }

    public int getMineTier() {
        return this.mineTier;
    }

    public void setMineTier(int tier) {
        this.mineTier = tier;
    }

    public int getResetTime() {
        return this.RESET_THRESHOLD;
    }

    public boolean contains(Player p) {
        return this.mainRegion.contains(Util.toWEVector(p.getLocation().toVector()));
    }

    public boolean fastMode() {
        return fastMode;
    }

    public String getResetStyle() {
        return resetStyle;
    }

    public void setResetStyle(String style) {
        this.resetStyle = style;
    }

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public Set<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public MineLocations getLocations() {
        return locations;
    }

    public int getUsersInMine() {
        Location min = locations.getRegion().getMinimumLocation();
        Location max = locations.getRegion().getMaximumLocation();
        Location loc = min.add(max);

        int total = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (loc.equals(online.getLocation())) {
                total++;
            }
        }
        return total;
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
        map.put("Reset-Percentage", this.resetPercentage);
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

    public boolean isFastMode() { return fastMode; }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
    }

    public void fillWEMultiple(List<ItemStack> stack) {

        final ICuboidSelection selection = (ICuboidSelection) locations.getWgRegion().getSelection();
        final WorldEditRegion miningRegion = new WorldEditRegion(
                Util.toWEVector(selection.getMinimumPoint()),
                Util.toWEVector(selection.getMaximumPoint()),
                mainRegion.getWorld()
        );

        //Could probably cache this but it's not very intensive
        //free any players who might be in the mine

        /*
            The Util.getOnlinePlayers part is linked to the caching system saves a few a milliseconds
         */

        for (Player player : Util.getOnlinePlayers()) {
            if (miningRegion.contains(Util.toWEVector(player.getLocation()))) {
                player.teleport(locations.getSpawnPoint());
                player.sendMessage(ChatColor.GREEN + "You've been teleported to the mine spawn point!");
            }
        }

        PrivateMines.getPlugin().getWeHook().fill(miningRegion, getMineBlocks(), isFastMode());

        this.nextResetTime = System.currentTimeMillis() + RESET_THRESHOLD;
    }

    public void fillWESingle(ItemStack itemStack) {

        final ICuboidSelection selection = (ICuboidSelection) locations.getWgRegion().getSelection();
        final WorldEditRegion miningRegion = new WorldEditRegion(
                Util.toWEVector(selection.getMinimumPoint()),
                Util.toWEVector(selection.getMaximumPoint()),
                mainRegion.getWorld()
        );

        //Could probably cache this but it's not very intensive
        //free any players who might be in the mine

        /*
            The Util.getOnlinePlayers part is linked to the caching system saves a few a milliseconds
         */

        for (Player player : Util.getOnlinePlayers()) {
            if (miningRegion.contains(Util.toWEVector(player.getLocation()))) {
                player.teleport(locations.getSpawnPoint());
                player.sendMessage(ChatColor.GREEN + "You've been teleported to the mine spawn point!");
            }
        }

        PrivateMines.getPlugin().getWeHook().fillSingle(miningRegion, itemStack, isFastMode());

        this.nextResetTime = System.currentTimeMillis() + RESET_THRESHOLD;
    }

    public boolean shouldReset() {
        return this.locations.getSpawnPoint().getChunk().isLoaded() && System.currentTimeMillis() >= nextResetTime;
    }

    public int getTotalBlocks() {
        final ICuboidSelection selection = (ICuboidSelection) locations.getWgRegion().getSelection();

        final WorldEditRegion miningRegion = new WorldEditRegion(
                Util.toWEVector(selection.getMinimumPoint()),
                Util.toWEVector(selection.getMaximumPoint()),
                mainRegion.getWorld()
        );

        Vector min1 = new Vector(
                miningRegion.getMinimumLocation().getBlockX(),
                miningRegion.getMinimumLocation().getBlockY(),
                miningRegion.getMinimumLocation().getBlockZ());

        Vector max1 = new Vector(
                miningRegion.getMaximumLocation().getBlockX(),
                miningRegion.getMaximumLocation().getBlockY(),
                miningRegion.getMaximumLocation().getBlockZ());

        CuboidRegion region = new CuboidRegion(min1, max1);
        return region.getArea();
    }

    public int getAirBlocks() {

        final ICuboidSelection selection = (ICuboidSelection) locations.getWgRegion().getSelection();
        final EditSession session = new EditSessionBuilder(FaweAPI.getWorld(locations.getRegion().getWorld().getName()))
                .fastmode(true)
                .build();

        final WorldEditRegion miningRegion = new WorldEditRegion(
                Util.toWEVector(selection.getMinimumPoint()),
                Util.toWEVector(selection.getMaximumPoint()),
                mainRegion.getWorld()
        );

        Vector min1 = new Vector(
                miningRegion.getMinimumLocation().getBlockX(),
                miningRegion.getMinimumLocation().getBlockY(),
                miningRegion.getMinimumLocation().getBlockZ());

        Vector max1 = new Vector(
                miningRegion.getMaximumLocation().getBlockX(),
                miningRegion.getMaximumLocation().getBlockY(),
                miningRegion.getMaximumLocation().getBlockZ());

        CuboidRegion region = new CuboidRegion(min1, max1);

        Set<Integer> airBlocks = new HashSet<>();
        airBlocks.add(0);

        return session.countBlock(region, airBlocks) + 1;
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
        boolean mineIsOpen = isOpen();
        setOpen(false);
        delete();

        PrivateMine newMine = PrivateMines.getPlugin().getFactory().create(
                Bukkit.getPlayer(owner),
                mineSchematic,
                Util.toLocation(mainRegion.getCenter(), locations.getSpawnPoint().getWorld()), false);

        fillWEMultiple(newMine.getMineBlocks());

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