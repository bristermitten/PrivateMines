//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

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
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
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
import me.bristermitten.privatemines.config.BlockType;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.MineLocations;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.data.SellNPC;
import me.bristermitten.privatemines.world.MineWorldManager;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class MineFactory {
    private final MineWorldManager manager;
    private final File file;
    private final PMConfig config;
    private final PrivateMines plugin;

    public MineFactory(PrivateMines plugin, MineWorldManager manager, PMConfig config) {
        this.plugin = plugin;
        this.manager = manager;
        this.file = new File(plugin.getDataFolder(), config.getSchematicName());
        this.config = config;
    }

    @SuppressWarnings("deprecation")
    public PrivateMine create(Player owner) {
        try {
            Location location = this.manager.nextFreeLocation();
            World world = FaweAPI.getWorld(location.getWorld().getName());
            ClipboardFormat format = ClipboardFormat.SCHEMATIC;
            EditSession e = new EditSessionBuilder(world).allowedRegionsEverywhere().limitUnlimited().build();
            Schematic schematic = format.load(new FileInputStream(file));
            if (schematic.getClipboard() == null) {
                plugin.getLogger().severe("Schematic does not have a Clipboard!");
                return null;
            }
            location.setY(schematic.getClipboard().getOrigin().getBlockY());
            Vector vector = BukkitUtil.toVector(location);
            schematic.paste(e, vector, false, true, null);
            Region r = Objects.requireNonNull(schematic.getClipboard()).getRegion();
            r.setWorld(world);
            r.shift(vector.subtract(schematic.getClipboard().getOrigin()));
            Location spawnLoc = null;
            Location npcLoc = null;
            Vector min = null;
            Vector max = null;
            Material spawnMaterial = this.config.getBlockTypes().get(BlockType.SPAWNPOINT);
            Material cornerMaterial = this.config.getBlockTypes().get(BlockType.CORNER);
            Material npcMaterial = this.config.getBlockTypes().get(BlockType.NPC);

            for (Vector pt : new FastIterator(r, e)) {
                Material type = Material.values()[world.getLazyBlock(pt).getType()];

                if (type == Material.AIR) continue;
                if (spawnLoc == null && type == spawnMaterial) {
                    spawnLoc = new Location(location.getWorld(), pt.getX(), pt.getY(), pt.getZ());
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
                    Location loc =
                            new Location(location.getWorld(), pt.getX(), pt.getY(), pt.getZ())
                                    .getBlock().getLocation();
                    loc.add(loc.getX() > 0 ? 0.5 : -0.5, 0.0, loc.getZ() > 0 ? 0.5 : -0.5);
                    npcLoc = loc;
                    world.setBlock(pt, new BaseBlock(0));
                }
            }

            if (spawnLoc == null) spawnLoc = location.getWorld().getHighestBlockAt(location).getLocation();
            if (min == null || max == null || min.equals(max)) {
                e.undo(e);
                throw new RuntimeException("Mine schematic did not define 2 corner blocks, mine cannot be" +
                        " formed");
            }
            if (npcLoc == null) npcLoc = spawnLoc;


            ProtectedRegion region = new ProtectedCuboidRegion(owner.getUniqueId().toString(),
                    r.getMinimumPoint().toBlockPoint(), r.getMaximumPoint().toBlockPoint());
            region.setFlag(DefaultFlag.BLOCK_BREAK, State.DENY);
            region.setFlag(DefaultFlag.BLOCK_PLACE, State.DENY);
            region.setFlag(DefaultFlag.MOB_SPAWNING, State.DENY);
            DefaultDomain domain = new DefaultDomain();
            domain.addPlayer(owner.getUniqueId());
            region.setOwners(domain);
            RegionManager regionManager =
                    WorldGuardPlugin.inst().getRegionManager(location.getWorld());
            regionManager.addRegion(region);
            ProtectedRegion mineRegion =
                    new ProtectedCuboidRegion(owner.getUniqueId().toString() + "-mine",
                            min.toBlockPoint(), max.toBlockPoint());
            mineRegion.setParent(region);
            mineRegion.setFlag(DefaultFlag.BLOCK_BREAK, State.ALLOW);
            regionManager.addRegion(mineRegion);
            MineLocations locations = new MineLocations(spawnLoc, min, max, mineRegion);
            NPC npc = new SellNPC(config.getNPCName(), config.getNpcSkin(), npcLoc,
                    owner.getUniqueId()).npc();

            return new PrivateMine(owner.getUniqueId(), true, this.config.getDefaultBlock(), r,
                    locations, region, npc.getUniqueId(), this.config.getTaxPercentage());

        } catch (WorldEditException | CircularInheritanceException | IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public MineWorldManager getManager() {
        return this.manager;
    }
}
