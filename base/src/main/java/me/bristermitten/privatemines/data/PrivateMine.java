package me.bristermitten.privatemines.data;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.api.PrivateMinesDeletionEvent;
import me.bristermitten.privatemines.api.PrivateMinesResetEvent;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.service.SchematicStorage;
import me.bristermitten.privatemines.util.Util;
import me.bristermitten.privatemines.worldedit.WorldEditRegion;
import me.bristermitten.privatemines.worldedit.WorldEditVector;
import me.clip.autosell.SellHandler;
import me.clip.autosell.objects.Shop;
import me.lucko.helper.Events;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PrivateMine implements ConfigurationSerializable {

    public static final String PLAYER_PLACEHOLDER = "{player}";
    private static final SchematicStorage schematicStorage = SchematicStorage.getInstance();
    /**
     * How far between a mine reset in milliseconds
     */
    private final int resetDelay;
    private final Set<UUID> bannedPlayers;
    private final Set<UUID> trustedPlayers;
    private final List<String> commands;
    private final UUID owner;
    private WorldEditRegion mainRegion;
    private MineLocations locations;
    private IWrappedRegion wgRegion;
    private UUID npcId;
    private boolean open;
    private boolean fastMode;
    private List<ItemStack> blocks;
    private double taxPercentage;
    private double resetPercentage;
    private MineSchematic<?> mineSchematic;
    private MineSchematic<?> upgradeSchematic;
    private long nextResetTime;
    private int mineTier;
    private String resetStyle;
    private final Location spawnLocation;

    // Is it even possible to get this down to the 7 max?
    // Yes.
    // How? We need most of them, don't we?

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
                       Location spawnLocation,
                       MineSchematic<?> mineSchematic) {
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
        this.resetDelay = (int) TimeUnit.MINUTES.toMillis(resetTime);
        this.mineTier = mineTier + 1;
        this.spawnLocation = spawnLocation;
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

        MineSchematic<?> schematic = schematicStorage.get(schematicName);

        if (schematic == null) {
            throw new IllegalArgumentException("Invalid Schematic " + schematicName);
        }

        Set<UUID> bannedPlayers = Optional.ofNullable((List<String>) map.get("BannedPlayers"))
                .map(bans -> bans.stream().map(UUID::fromString).collect(Collectors.toSet()))
                .orElse(new HashSet<>());

        Set<UUID> trustedPlayers = Optional.ofNullable((List<String>) map.get("TrustedPlayers"))
                .map(trusted -> trusted.stream().map(UUID::fromString).collect(Collectors.toSet())).orElse(new HashSet<>());

        String shopName = "shop-" + owner;
        Shop shop = new Shop(shopName);
        SellHandler.addShop(shop);

        Location spawnLocation = locations.getSpawnPoint();

        return new PrivateMine(owner, bannedPlayers, trustedPlayers, commands, open, fastMode, blocks, mainRegion, locations,
                wgRegion, npcId, taxPercentage, resetPercentage, resetTime, mineTier, spawnLocation, schematic);
    }

    public double getTaxPercentage() {
        if (this.taxPercentage == 0) {
            return 0;
        } else {
            return this.taxPercentage;
        }
    }

    public void setTaxPercentage(double amount) {
        this.taxPercentage = amount;
    }

    public boolean hasTax() {
        return this.taxPercentage != 0;
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

    public void setMineBlocks(List<ItemStack> itemStack) {
        this.blocks = itemStack;
    }

    public List<String> getMineBlocksString() {
        ArrayList<String> blocksString = new ArrayList<>();
        for (ItemStack itemStack : getMineBlocks()) {
            blocksString.add(itemStack.getType().name());
        }
        return blocksString;
    }


    public void addMineBlock(ItemStack itemStack) {
        this.blocks.add(itemStack);
    }

    public void removeMineBlock(ItemStack itemStack) {
        this.blocks.remove(itemStack);
    }

    public int getMineTier() {
        return this.mineTier;
    }

    public void setMineTier(int tier) {
        this.mineTier = tier;
    }

    public int getResetTime() {
        return this.resetDelay;
    }

    public UUID getNPCUUID() {
        return this.npcId;
    }

    public boolean contains(Player p) {
        return this.mainRegion.contains(Util.toWEVector(p.getLocation().toVector()));
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

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public @NotNull Map<String, Object> serialize() {
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
        map.put("Reset-Delay", this.resetDelay);
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
        return Objects.requireNonNull(owner);
    }

    public Player getOwnerPlayer() {
        return Objects.requireNonNull(Bukkit.getPlayer(owner));
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

    public boolean isFastMode() {
        return fastMode;
    }

    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
    }

    public void fillMine() {
        fillMine(getMineBlocks());
    }

    public void fillMine(List<ItemStack> blocksToFill) {
        final ICuboidSelection selection = (ICuboidSelection) locations.getWgRegion().getSelection();
        final WorldEditRegion miningRegion = new WorldEditRegion(
                Util.toWEVector(selection.getMinimumPoint()),
                Util.toWEVector(selection.getMaximumPoint()),
                mainRegion.getWorld()
        );

        for (Player player : Util.getOnlinePlayers()) {
            if (miningRegion.contains(Util.toWEVector(player.getLocation()))) {
                player.teleport(locations.getSpawnPoint());
                player.sendMessage(ChatColor.GREEN + "You've been teleported to the mine spawn point!");
            }
        }

        PrivateMinesResetEvent privateMinesResetEventEvent = new PrivateMinesResetEvent(
                this.getOwnerPlayer(),
                this,
                blocksToFill
        );

        Events.call(privateMinesResetEventEvent);

        if (privateMinesResetEventEvent.isCancelled()) {
            return;
        }

        PrivateMines.getPlugin().getWeHook().fill(miningRegion, blocksToFill, isFastMode());

        this.nextResetTime = System.currentTimeMillis() + resetDelay;
    }

    public void fillWESingle(ItemStack itemStack) {
        fillMine(Collections.singletonList(itemStack));
    }

    public boolean shouldReset() {
        return this.locations.getSpawnPoint().getChunk().isLoaded() && System.currentTimeMillis() >= nextResetTime;
    }

    public double getTax() {
        return taxPercentage;
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

        PrivateMinesDeletionEvent privateMinesDeletionEvent
                = new PrivateMinesDeletionEvent(this);

        Events.call(privateMinesDeletionEvent);

        /*
            Removes mine blocks + shell also
         */

        PrivateMines.getPlugin().getWeHook().fillAir(mainRegion, isFastMode());
        PrivateMines.getPlugin().getWeHook().fillAir(miningRegion, isFastMode());
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

    public void setMineSchematic(MineSchematic<?> mineSchematic, Location location, Player player) {
        boolean mineIsOpen = isOpen();
        setOpen(false);

        PrivateMine oldMine = PrivateMines.getPlugin().getStorage().get(player);

        if (oldMine != null) {
            WorldEditRegion oldRegion = oldMine.mainRegion;

            PrivateMines.getPlugin().getWeHook().fillAir(oldRegion, true);
            oldMine.delete();
        }

        PrivateMine newMine = PrivateMines.getPlugin().getFactory().create(
                getOwnerPlayer(),
                mineSchematic,
                location,
                false);

        this.locations = newMine.locations;
        this.mainRegion = newMine.mainRegion;
        this.wgRegion = newMine.wgRegion;
        this.npcId = newMine.npcId;
        this.mineSchematic = mineSchematic;
        setOpen(mineIsOpen);
    }

    public void upgradeMine(Player player) {
        int tier;
        PrivateMine currentMine = PrivateMines.getPlugin().getStorage().get(player);
        if (currentMine != null) {
            Location currentMineLocation = currentMine.getSpawnLocation();
            tier = currentMine.getMineTier();
            int newTier = tier + 1;

            Collection<MineSchematic<?>> mineSchematics = schematicStorage.getAll();

            for (MineSchematic schema : mineSchematics) {
                if (schema.getTier() == newTier) {
                    upgradeSchematic = schema;
                }
            }

            final ICuboidSelection selection = (ICuboidSelection) locations.getWgRegion().getSelection();

            final WorldEditRegion miningRegion = new WorldEditRegion(
                    Util.toWEVector(selection.getMinimumPoint()),
                    Util.toWEVector(selection.getMaximumPoint()),
                    mainRegion.getWorld()
            );

            if (upgradeSchematic == null) {
                Bukkit.getLogger().warning("Error upgrading " + player.getName() + "'s Mine due the schematic being null!");
                return;
            }
            if (getNPCUUID() == null) {
                Bukkit.getLogger().warning("Error deleting " + player.getName() + "'s Mine NPC due to it being null.");
                return;
            }
            CitizensAPI.getNPCRegistry().getByUniqueId(getNPCUUID()).destroy();

            PrivateMines.getPlugin().getWeHook().fillAir(miningRegion, isFastMode());
            currentMine.setMineSchematic(upgradeSchematic, currentMineLocation, player);
            currentMine.getLocations().setSpawnPoint(currentMineLocation);
            currentMine.teleport(player);
            currentMine.setMineTier(newTier);
        }
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
