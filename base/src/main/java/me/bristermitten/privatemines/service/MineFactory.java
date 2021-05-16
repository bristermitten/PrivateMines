package me.bristermitten.privatemines.service;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.api.PrivateMinesCreationEvent;
import me.bristermitten.privatemines.config.BlockType;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.MineLocations;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.data.citizens.autosell.AutoSellNPC;
import me.bristermitten.privatemines.data.citizens.ultraprisoncore.UltraPrisonCoreNPC;
import me.bristermitten.privatemines.util.Util;
import me.bristermitten.privatemines.world.MineWorldManager;
import me.bristermitten.privatemines.worldedit.MineFactoryCompat;
import me.bristermitten.privatemines.worldedit.WorldEditRegion;
import me.bristermitten.privatemines.worldedit.WorldEditVector;
import me.lucko.helper.Events;
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

    private static final String NAME_PLACEHOLDER = "{name}";
    private static final String DISPLAYNAME_PLACEHOLDER = "{displayname}";
    private static final String UUID_PLACEHOLDER = "{uuid}";
    protected final PMConfig config;

    protected final PrivateMines plugin;
    private final MineWorldManager manager;
    private final MineFactoryCompat<S> compat;
    UUID npcUUID;

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

    //TODO this method is way too complex. Refactor ASAP

    /**
     * Creates a private mine, pastes the schematic, sets the spawn location and fills the mine.
     *
     * @param owner         The private mine owner
     * @param mineSchematic The schematic to paste for the mine
     * @param origin        Where to paste the mine. This is the exact location where the schematic should be pasted (where the player would be standing if doing //paste)
     * @param isNew         If this is a new private mine or upgrading a new one.
     *                      Much of the functionality in this method does not belong here and should be refactored immediately.
     */
    @SuppressWarnings("deprecation")
    public PrivateMine create(Player owner, M mineSchematic, final Location origin, @Deprecated boolean isNew) {

        WorldEditRegion region = compat.pasteSchematic(mineSchematic.getSchematic(), origin);

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
        World world = origin.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("World for provided location was null");
        }
        for (Iterator<WorldEditVector> iterator = compat.loop(region).iterator(); iterator.hasNext(); ) {
            WorldEditVector pt = iterator.next();
            final Block blockAt = world.getBlockAt((int) pt.getX(), (int) pt.getY(), (int) pt.getZ());
            Material type = blockAt.getType();
            byte data = blockAt.getData();
            if (type == Material.AIR || type.name().equals("LEGACY_AIR")) continue;

            if (spawnLoc == null && type == spawnMaterial.getType() && dataMatches(data, spawnMaterial.getDurability())) {
                spawnLoc = new Location(origin.getWorld(), pt.getX() + 0.5, pt.getY() + 0.5, pt.getZ() + 0.5);
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
                npcLoc = new Location(origin.getWorld(), pt.getX(), pt.getY(), pt.getZ()).getBlock().getLocation();
                npcLoc.add(npcLoc.getX() > 0 ? 0.5 : -0.5, 0.0, npcLoc.getZ() > 0 ? 0.5 : -0.5);
                blockAt.setType(Material.AIR); //Clear the block
            }
        }

        if (min == null || max == null || min.equals(max)) {
            throw new IllegalArgumentException("Mine schematic did not define 2 distinct corner blocks, mine cannot be formed");
        }
        if (spawnLoc == null && origin.getWorld() != null) {
            spawnLoc = origin.getWorld().getHighestBlockAt(origin).getLocation();
            plugin.getLogger().warning(() -> "No spawn block was defined, so the schematic root will be used. Searching for " + spawnMaterial);
        }
        if (npcLoc == null) {
            npcLoc = spawnLoc;
            plugin.getLogger().warning(() -> "No npc spawnpoint was defined, so the schematic spawn will be used. Searching for " + npcMaterial);
        }

        WorldEditRegion mainRegion = new WorldEditRegion(region.getMinimumPoint(), region.getMaximumPoint(), origin.getWorld());
        IWrappedRegion worldGuardRegion = createMainWorldGuardRegion(owner, mainRegion);

        WorldEditRegion miningRegion = new WorldEditRegion(min, max, origin.getWorld());
        IWrappedRegion mineRegion = createMineWorldGuardRegion(owner, miningRegion, worldGuardRegion);

        MineLocations locations = new MineLocations(spawnLoc, min, (max), mineRegion);

        //TODO Make shop system somehow?

        final PrivateMine privateMine = new PrivateMine(
                owner.getUniqueId(),
                new HashSet<>(),
                config.getDefaultMineBlocks(),
                mainRegion,
                locations,
                worldGuardRegion,
                config.getMineTier());

        privateMine.fillMine();
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

        if (plugin.isAutoSellEnabled()) {
            npcUUID = createAutoSellMineNPC(owner, npcLoc);
            locations.setNpcLocation(npcLoc);
        } else if (plugin.isUltraPrisonCoreEnabled()) {
            npcUUID = createUltraPrisonCoreMineNPC(owner, npcLoc);
        }

        final List<String> commandsToRun;
        if (isNew) {
            commandsToRun = firstTimeCommands;
        } else {
            commandsToRun = commands;
        }
        for (String command : commandsToRun) {
            final String formattedCommand =
                    command.replace(NAME_PLACEHOLDER, owner.getName())
                            .replace(DISPLAYNAME_PLACEHOLDER, owner.getDisplayName())
                            .replace(UUID_PLACEHOLDER, owner.getUniqueId().toString());

            Bukkit.dispatchCommand(console, formattedCommand);
        }

        PrivateMinesCreationEvent privateMinesCreationEvent
                = new PrivateMinesCreationEvent(owner, privateMine,
                config.getMineBlocks(), config.getTaxPercentage(), config.getResetDelay());
        Events.call(privateMinesCreationEvent);
        return privateMine;
    }

    //TODO  These 2 methods do pretty much the exact same thing and should be abstracted out (all npc functionality should be)
    private UUID createAutoSellMineNPC(Player owner, Location npcLoc) {
        UUID npcUUID;
        if (plugin.isNpcsEnabled()) {
            npcUUID = AutoSellNPC.createSellNPC(
                    config.getNPCName(),
                    owner.getName(),
                    npcLoc,
                    owner.getUniqueId()).getUniqueId();
        } else {
            npcUUID = UUID.randomUUID(); //This means we can fail gracefully when the NPC doesn't exist
        }
        return npcUUID;
    }

    private UUID createUltraPrisonCoreMineNPC(Player owner, Location npcLoc) {
        UUID npcUUID;
        if (plugin.isNpcsEnabled()) {
            npcUUID = UltraPrisonCoreNPC.createUPCSellNPC(
                    config.getNPCName(),
                    owner.getName(),
                    npcLoc,
                    owner.getUniqueId()
            ).getUniqueId();
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
        final WorldGuardWrapper w = WorldGuardWrapper.getInstance();
        Stream.of(
                w.getFlag("mob-spawning", WrappedState.class)
        ).filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(flag -> region.setFlag(flag, WrappedState.DENY));

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
