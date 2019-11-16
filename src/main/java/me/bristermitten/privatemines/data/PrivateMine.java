package me.bristermitten.privatemines.data;

import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

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
    private BukkitTask task;
    private ProtectedRegion wgRegion;

    public PrivateMine(UUID owner, boolean open, Material block, Region region, MineLocations locations,
                       ProtectedRegion wgRegion) {
        this.owner = owner;
        this.open = open;
        this.region = region;
        this.locations = locations;
        this.editSession =
                new EditSessionBuilder(region.getWorld()).allowedRegions(new Region[]{region, locations.getRegion()})
                        .build();
        this.block = block;
        this.wgRegion = wgRegion;
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

        ProtectedRegion wgRegion =
                WorldGuardPlugin.inst().getRegionManager(locations.getSpawnPoint().getWorld()).getRegion(owner.toString());
        return new PrivateMine(owner, open, block, region, locations, wgRegion);
    }

    public Material getBlock() {
        return block;
    }

    public void setBlock(Material block) {
        if (!block.isBlock()) return;
        this.block = block;
        fill(block);
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
        editSession.setBlocks(locations.getRegion(), new BaseBlock(m.getId()));
        editSession.flushQueue();

        if (task != null)
            task.cancel();
        task = Bukkit.getScheduler().runTaskLater(PrivateMines.getPlugin(PrivateMines.class), () -> fill(block),
                60 * 20);
    }

    public boolean isOpen() {
        return open;
    }

    public void delete() {
        if (task != null)
            task.cancel();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (wgRegion.contains(Util.toVector(player.getLocation().toVector()))) {
                player.performCommand("spawn");
            }
        }
        editSession.setBlocks(region, new BaseBlock(0));
        editSession.flushQueue();
    }
}
