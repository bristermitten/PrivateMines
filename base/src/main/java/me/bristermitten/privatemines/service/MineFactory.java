package me.bristermitten.privatemines.service;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.BlockType;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.MineLocations;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.data.SellNPC;
import me.bristermitten.privatemines.util.Util;
import me.bristermitten.privatemines.world.MineWorldManager;
import me.bristermitten.privatemines.worldedit.MineFactoryCompat;
import me.bristermitten.privatemines.worldedit.WorldEditRegion;
import me.bristermitten.privatemines.worldedit.WorldEditVector;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.WrappedState;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class MineFactory<M extends MineSchematic<S>, S> {

    public static final String DEFAULT_MINE_OPEN_KEY = "Default-Open";
    protected final PMConfig config;
    protected final PrivateMines plugin;
    private final MineWorldManager manager;
    private final MineFactoryCompat<S> compat;

    // List the placeholders here for re-use.

    private static final String NAME_PLACEHOLDER = "{name}";
    private static final String DISPLAYNAME_PLACEHOLDER = "{displayname}";
    private static final String UUID_PLACEHOLDER = "{uuid}";

    public MineFactory(PrivateMines plugin, MineWorldManager manager, PMConfig config, MineFactoryCompat<S> compat) {
        this.plugin = plugin;
        this.manager = manager;
        this.config = config;
        this.compat = compat;
    }

    public PrivateMine create(Player owner, M mineSchematic, boolean isNew) {
        return create(owner, mineSchematic, manager.nextFreeLocation(), isNew);
    }

    private boolean dataMatches(byte data, short expectedData) {
        if (data == 0) {
            return expectedData == 0;
        }
        return expectedData % data == 0;
    }

    /*
    Creates the private mine, pastes the schematic, sets the spawn location and fills the mine.
     */
    @SuppressWarnings("deprecation")
    public PrivateMine create(Player owner, M mineSchematic, final Location location, boolean isNew) {

        WorldEditRegion region = compat.pasteSchematic(mineSchematic.getSchematic(), location);

        Location spawnLoc = null;

        Location npcLoc = null;
        WorldEditVector min = null;
        WorldEditVector max = null;

        Map<BlockType, ItemStack> blockTypes = config.getBlockTypes();
        List<String> firstTimeCommands = this.config.getFirstTimeCommands();
        List<String> commands = this.config.getCommands();

        ItemStack spawnMaterial = blockTypes.get(BlockType.SPAWNPOINT);
        ItemStack cornerMaterial = blockTypes.get(BlockType.CORNER);
        ItemStack npcMaterial = blockTypes.get(BlockType.NPC);

        /*
	    Loops through all of the blocks to find the spawn location block, replaces it with air and sets the location
	    in the config.
	    */
        World world = location.getWorld();
        for (WorldEditVector pt : compat.loop(region)) {
            final Block blockAt = world.getBlockAt((int) pt.getX(), (int) pt.getY(), (int) pt.getZ());
            Material type = blockAt.getType();
            byte data = blockAt.getData();
            if (type == Material.AIR || type.name().equals("LEGACY_AIR")) continue;

            if (spawnLoc == null && type == spawnMaterial.getType() && dataMatches(data, spawnMaterial.getDurability())) {
                spawnLoc = new Location(location.getWorld(), pt.getX() + 0.5, pt.getY() + 0.5, pt.getZ() + 0.5);
                Block block = spawnLoc.getBlock();
                if (block.getState().getData() instanceof Directional) {
                    spawnLoc.setYaw(Util.getYaw(((Directional) block.getState().getData()).getFacing()));
                }
                blockAt.setType(Material.AIR);
                continue;
            }

            /*
			Loops through all the blocks finding the corner block.
			*/
            if (type == cornerMaterial.getType() && dataMatches(data, cornerMaterial.getDurability())) {
                if (min == null) {
                    min = pt.copy();
                    continue;
                }
                if (max == null) {
                    max = pt.copy();
                    continue;
                }
                plugin.getLogger().warning("Mine schematic contains >2 corner blocks!");
                continue;
            }

            /*
			Loops through all the blocks finding the NPC block and sets the NPC location.
			*/
            if (type == npcMaterial.getType() && dataMatches(data, npcMaterial.getDurability())) {
                npcLoc = new Location(location.getWorld(), pt.getX(), pt.getY(), pt.getZ()).getBlock().getLocation();
                npcLoc.add(npcLoc.getX() > 0 ? 0.5 : -0.5, 0.0, npcLoc.getZ() > 0 ? 0.5 : -0.5);
                blockAt.setType(Material.AIR); //Clear the block
            }
        }

        if (min == null || max == null || min.equals(max)) {
            throw new IllegalArgumentException("Mine schematic did not define 2 distinct corner blocks, mine cannot be formed");
        }
        if (spawnLoc == null) {
            spawnLoc = location.getWorld().getHighestBlockAt(location).getLocation();
            plugin.getLogger().warning(() -> "No spawn block was defined, so the schematic root will be used. Searching for " + spawnMaterial);
        }
        if (npcLoc == null) {
            npcLoc = spawnLoc;
            plugin.getLogger().warning(() -> "No npc spawnpoint was defined, so the schematic spawn will be used. Searching for " + npcMaterial);
        }

        WorldEditRegion mainRegion = new WorldEditRegion((region.getMinimumPoint()), region.getMaximumPoint(), location.getWorld());
        IWrappedRegion worldGuardRegion = createMainWorldGuardRegion(owner, mainRegion);

        WorldEditRegion miningRegion = new WorldEditRegion(min, max, location.getWorld());
        IWrappedRegion mineRegion = createMineWorldGuardRegion(owner, miningRegion, worldGuardRegion);

        MineLocations locations = new MineLocations(spawnLoc, min, (max), mineRegion);

        UUID npcUUID = createMineNPC(owner, npcLoc);

        //TODO Make shop system somehow?

        final PrivateMine privateMine = new PrivateMine(
                owner.getUniqueId(),
                new HashSet<>(),
                new HashSet<>(),
                commands,
                plugin.getConfig().getBoolean(DEFAULT_MINE_OPEN_KEY, true),
                config.getDefaultBlock(),
                mainRegion,
                locations,
                worldGuardRegion,
                npcUUID,
                config.getTaxPercentage(),
                config.getResetPercentage(),
                config.getResetDelay(),
                config.getMineTier(),
                mineSchematic,
                plugin.getStorage()
                );

        privateMine.fillWE();
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();


        if (isNew) {
            for (String s : firstTimeCommands) {
                s = s.replace(NAME_PLACEHOLDER, owner.getName());
                s = s.replace(DISPLAYNAME_PLACEHOLDER, owner.getDisplayName());
                s = s.replace(UUID_PLACEHOLDER, owner.getPlayer().getUniqueId().toString());
                Bukkit.dispatchCommand(console, s);
            }

            if (config.getDefaultBlock() == null) {
                Bukkit.getLogger().info("Default block was null!");
            }
        } else {
            for (String s : commands) {
                s = s.replace(NAME_PLACEHOLDER, owner.getName());
                s = s.replace(DISPLAYNAME_PLACEHOLDER, owner.getDisplayName());
                s = s.replace(UUID_PLACEHOLDER, owner.getPlayer().getUniqueId().toString());
                Bukkit.dispatchCommand(console, s);
            }
        }
        return privateMine;
}

    private UUID createMineNPC(Player owner, Location npcLoc) {
        UUID npcUUID;
        if (plugin.isNpcsEnabled()) {
            npcUUID = SellNPC.createSellNPC(
                    config.getNPCName(),
                    owner.getName(),
                    npcLoc,
                    owner.getUniqueId()).getUniqueId();
        } else {
            npcUUID = UUID.randomUUID(); //This means we can fail gracefully when the NPC doesn't exist
        }
        return npcUUID;
    }

    @NotNull
    protected IWrappedRegion createMainWorldGuardRegion(Player owner, WorldEditRegion r) {
        IWrappedRegion region = WorldGuardWrapper.getInstance().addCuboidRegion(
                owner.getUniqueId().toString(),
                r.getMinimumLocation(),
                r.getMaximumLocation())
                .orElseThrow(() -> new RuntimeException("Could not create Main WorldGuard region"));

        region.getOwners().addPlayer(owner.getUniqueId());

        setMainFlags(region);
        return region;
    }

    @NotNull
    protected IWrappedRegion createMineWorldGuardRegion(Player owner, WorldEditRegion region, IWrappedRegion parent) {
        IWrappedRegion mineRegion = WorldGuardWrapper.getInstance().addCuboidRegion(

                config.getMineRegionNameFormat().replace(UUID_PLACEHOLDER, owner.getUniqueId().toString()),
                region.getMinimumLocation(),
                region.getMaximumLocation())
                .orElseThrow(() -> new RuntimeException("Could not create Mine WorldGuard region"));


        parent.getOwners().getPlayers().forEach(uuid -> mineRegion.getOwners().addPlayer(uuid));
        mineRegion.setPriority(1);
        setMineFlags(mineRegion);
        return mineRegion;
    }

    public void setMineFlags(IWrappedRegion region) {
        WorldGuardWrapper.getInstance().getFlag("block-break", WrappedState.class)
                .ifPresent(flag -> region.setFlag(flag, WrappedState.ALLOW));
    }

    public void setMainFlags(IWrappedRegion region) {
        final WorldGuardWrapper w = WorldGuardWrapper.getInstance();
        Stream.of(
                w.getFlag("block-place", WrappedState.class),
                w.getFlag("block-break", WrappedState.class),
                w.getFlag("mob-spawning", WrappedState.class)
        ).filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(flag -> region.setFlag(flag, WrappedState.DENY));
    }

    public MineWorldManager getManager() {
        return manager;
    }
}