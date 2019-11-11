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
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.BlockType;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.MineLocations;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.world.MineWorldManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static org.bukkit.Material.AIR;

public class MineFactory {
    private final MineWorldManager manager;
    private final File file;
    private final Map<BlockType, Material> blocks;
    private final PrivateMines plugin;

    public MineFactory(PrivateMines plugin, MineWorldManager manager, PMConfig config) {
        this.plugin = plugin;
        this.manager = manager;
        this.file = new File(plugin.getDataFolder(), config.getSchematicName());
        this.blocks = config.getBlockTypes();
    }

    public PrivateMine create(Player owner) {
        try {
            Location location = manager.nextFreeLocation();
            World world = FaweAPI.getWorld(location.getWorld().getName());
            ClipboardFormat format = ClipboardFormat.SCHEMATIC;
            EditSession e = new EditSessionBuilder(world).allowedRegionsEverywhere().limitUnlimited().build();
            Schematic schematic = format.load(new FileInputStream(file));
            location.setY(schematic.getClipboard().getOrigin().getBlockY());
            Vector vector = BukkitUtil.toVector(location);

            schematic.paste(e, vector, false, true, null);
            Region r = Objects.requireNonNull(schematic.getClipboard()).getRegion();
            r.setWorld(world);
            r.shift(vector.subtract(r.getCenter()));

            Location spawnLoc = null;
            Vector[] corners = new Vector[2];
            Material spawnMaterial = blocks.get(BlockType.SPAWNPOINT);
            Material cornerMaterial = blocks.get(BlockType.CORNER);

            for (Vector pt : new FastIterator(r, e)) {
                Material type = Material.values()[world.getLazyBlock(pt).getType()];
                if (type == AIR) continue;
                if (type.name().contains("SIGN")) {
                    System.out.println(type);
                }
                if (spawnLoc == null && type == spawnMaterial) {
                    spawnLoc = new Location(location.getWorld(), pt.getX(), pt.getY(), pt.getZ());
                    world.setBlock(pt, new BaseBlock(0));
                    continue;
                }
                if (type == cornerMaterial) {
                    if (corners[0] == null) {
                        corners[0] = pt;
                        world.setBlock(pt, new BaseBlock(0));
                        continue;
                    }
                    if (corners[1] == null) {
                        corners[1] = pt;
                        world.setBlock(pt, new BaseBlock(0));
                        continue;
                    }
                    plugin.getLogger().warning("Mine schematic contains >2 corner blocks!");
                }
            }

            if (spawnLoc == null) spawnLoc = location.getWorld().getHighestBlockAt(location).getLocation();
            if (corners[0] == null || corners[1] == null) {
                e.undo(e);
                throw new RuntimeException("Mine schematic did not define 2 corner blocks, mine cannot be formed");
            }
            CuboidRegion region = new CuboidRegion(corners[0], corners[1]);
            Vector min = region.getMinimumPoint().setY(0);
            Vector max = region.getMaximumPoint().subtract(0, 1, 0);

            MineLocations locations = new MineLocations(spawnLoc, min, max);
            return new PrivateMine(owner.getUniqueId(), true, r, locations);
        } catch (IOException | WorldEditException e) {
            e.printStackTrace();
        }
        return null;
    }
}
