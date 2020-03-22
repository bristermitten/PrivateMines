package me.bristermitten.privatemines.service;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.boydti.fawe.object.visitor.FastIterator;
import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.Util;
import me.bristermitten.privatemines.config.BlockType;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.MineLocations;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.data.SellNPC;
import me.bristermitten.privatemines.world.MineWorldManager;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Directional;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MineFactory {
	public static final boolean DEFAULT_MINE_OPEN = true;
	private final MineWorldManager manager;
	private final PMConfig config;
	private final PrivateMines plugin;

	private final EditSession editSession;
	private final World world;

	public MineFactory(PrivateMines plugin, MineWorldManager manager, PMConfig config) {
		this.plugin = plugin;
		this.manager = manager;
		this.config = config;

		world = FaweAPI.getWorld(manager.getMinesWorld().getName());

		this.editSession = new EditSessionBuilder(world).allowedRegionsEverywhere().limitUnlimited()
				.fastmode(true).build();
	}

	public PrivateMine create(Player owner, MineSchematic mineSchematic) {
		return create(owner, mineSchematic, manager.nextFreeLocation());
	}

	@SuppressWarnings("deprecation")
	public PrivateMine create(Player owner, MineSchematic mineSchematic, final Location location) {
		try {
			final Schematic schematic = mineSchematic.getSchematic();

			final Clipboard clipboard = schematic.getClipboard();

			if (clipboard == null) {
				throw new IllegalStateException("Schematic does not have a Clipboard! This should never happen!");
			}

			location.setY(clipboard.getOrigin().getBlockY());

			final Vector centerVector = BukkitUtil.toVector(location);
			schematic.paste(editSession, centerVector, false, true, null);

			Region region = clipboard.getRegion();
			region.setWorld(world);
			region.shift(centerVector.subtract(clipboard.getOrigin()));

			Location spawnLoc = null;
			Location npcLoc = null;
			Vector min = null;
			Vector max = null;

			Map<BlockType, Material> blockTypes = config.getBlockTypes();
			Material spawnMaterial = blockTypes.get(BlockType.SPAWNPOINT);
			Material cornerMaterial = blockTypes.get(BlockType.CORNER);
			Material npcMaterial = blockTypes.get(BlockType.NPC);


			for (Vector pt : new FastIterator(region, editSession)) {
				Material type = Material.values()[world.getLazyBlock(pt).getType()];
				if (type == Material.AIR) continue;

				if (spawnLoc == null && type == spawnMaterial) {
					spawnLoc = new Location(location.getWorld(), pt.getX() + 0.5, pt.getY() + 0.5, pt.getZ() + 0.5);
					Block block = spawnLoc.getBlock();
					if (block.getState().getData() instanceof Directional) {
						spawnLoc.setYaw(Util.getYaw(((Directional) block.getState().getData()).getFacing()));
					}
					world.setBlock(pt, new BaseBlock(0));
					continue;
				}

				if (type == cornerMaterial) {
					if (min == null) {
						min = new Vector(pt);
						continue;
					}
					if (max == null) {
						max = new Vector(pt);
						continue;
					}
					plugin.getLogger().warning("Mine schematic contains >2 corner blocks!");
					continue;
				}
				if (type == npcMaterial) {
					npcLoc =
							new Location(location.getWorld(), pt.getX(), pt.getY(), pt.getZ()).getBlock().getLocation();
					npcLoc.add(npcLoc.getX() > 0 ? 0.5 : -0.5, 0.0, npcLoc.getZ() > 0 ? 0.5 : -0.5);
					world.setBlock(pt, new BaseBlock(0));
				}
			}

			if (spawnLoc == null) spawnLoc = location.getWorld().getHighestBlockAt(location).getLocation();

			if (min == null || max == null || min.equals(max)) {
				throw new RuntimeException("Mine schematic did not define 2 corner blocks, mine cannot be formed");
			}

			if (npcLoc == null) npcLoc = spawnLoc;

			RegionManager regionManager =
					WorldGuardPlugin.inst().getRegionManager(location.getWorld());

			ProtectedRegion worldGuardRegion = createMainWorldGuardRegion(owner, region);
			regionManager.addRegion(worldGuardRegion);

			ProtectedRegion mineRegion = createMineWorldGuardRegion(owner, min, max, worldGuardRegion);

			regionManager.addRegion(mineRegion);

			MineLocations locations = new MineLocations(spawnLoc, min, max, mineRegion);

			NPC npc = SellNPC.createSellNPC(
					config.getNPCName(),
					owner.getName(),
					npcLoc,
					owner.getUniqueId());

			return new PrivateMine(
					owner.getUniqueId(),
					DEFAULT_MINE_OPEN,
					config.getDefaultBlock(),
					region,
					locations,
					worldGuardRegion,
					npc.getUniqueId(),
					config.getTaxPercentage(),
					mineSchematic);

		} catch (WorldEditException | CircularInheritanceException e) {
			throw new RuntimeException(e);
		}
	}

	@NotNull
	private ProtectedRegion createMainWorldGuardRegion(Player owner, Region r) {
		ProtectedRegion region = new ProtectedCuboidRegion(
				owner.getUniqueId().toString(),
				r.getMinimumPoint().toBlockPoint(),
				r.getMaximumPoint().toBlockPoint());

		region.setFlag(DefaultFlag.BLOCK_BREAK, State.DENY);
		region.setFlag(DefaultFlag.BLOCK_PLACE, State.DENY);
		region.setFlag(DefaultFlag.MOB_SPAWNING, State.DENY);
		region.setFlag(DefaultFlag.INTERACT, State.ALLOW);

		DefaultDomain domain = new DefaultDomain();
		domain.addPlayer(owner.getUniqueId());
		region.setOwners(domain);
		return region;
	}

	@NotNull
	private ProtectedRegion createMineWorldGuardRegion(Player owner, Vector min, Vector max, ProtectedRegion parent) throws CircularInheritanceException {
		ProtectedRegion mineRegion = new ProtectedCuboidRegion(
				owner.getUniqueId().toString() + "-mine",
				min.toBlockPoint(),
				max.toBlockPoint());

		mineRegion.setParent(parent);
		mineRegion.setOwners(parent.getOwners());
		mineRegion.setPriority(1);
		mineRegion.setFlag(DefaultFlag.BLOCK_BREAK, State.ALLOW);
		return mineRegion;
	}

	public MineWorldManager getManager() {
		return manager;
	}
}
