package me.bristermitten.privatemines.data;

import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.bristermitten.privatemines.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class PrivateMine implements ConfigurationSerializable {
    private UUID owner;
    private boolean open;
    private Region region;
    private MineLocations locations;
    private Material block;
    private EditSession editSession;

    public PrivateMine(UUID owner, boolean open, Material block, Region region, MineLocations locations) {
        this.owner = owner;
        this.open = open;
        this.region = region;
        this.locations = locations;
        this.editSession = new EditSessionBuilder(region.getWorld()).allowedRegions(new Region[]{locations.getRegion()})
                .build();

        this.block = block;
        fill(block);

    }

    public static PrivateMine deserialize(Map<String, Object> map) {
        UUID owner = UUID.fromString((String) map.get("Owner"));
        boolean open = (boolean) map.get("Open");
        Material block = Material.matchMaterial((String) map.get("Block"));
        com.sk89q.worldedit.Vector corner1 =
                Util.toVector(Vector.deserialize((Map<String, Object>) map.get("Corner1")));
        com.sk89q.worldedit.Vector corner2 =
                Util.toVector(Vector.deserialize((Map<String, Object>) map.get("Corner2")));
        MineLocations locations = MineLocations.deserialize((Map<String, Object>) map.get("Locations"));
        CuboidRegion region = new CuboidRegion(corner1, corner2);
        region.setWorld(new BukkitWorld(locations.getSpawnPoint().getWorld()));
        return new PrivateMine(owner, open, block, region, locations);
    }

    public Material getBlock() {
        return block;
    }

    public void getBlock(Material block) {
        this.block = block;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();
        map.put("Owner", owner.toString());
        map.put("Open", open);
        map.put("Block", block.name());
        map.put("Locations", locations.serialize());
        map.put("Corner1", Util.toVector(region.getMinimumPoint()).serialize());
        map.put("Corner2", Util.toVector(region.getMaximumPoint()).serialize());
        return map;
    }

    public void teleport(Player p) {
        p.teleport(locations.getSpawnPoint());
    }

    public void teleport() {
        Player player = Bukkit.getPlayer(owner);
        if (player != null)
            teleport(player);
    }

    public UUID getOwner() {
        return owner;
    }

    public void fill(Material m) {
        try {
            editSession.setBlocks(locations.getRegion(), new BaseBlock(m.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            editSession.flushQueue();
        }
    }
}
