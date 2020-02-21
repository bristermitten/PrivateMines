//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.data;

import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.Util;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

public class PrivateMine implements ConfigurationSerializable {
    private final UUID owner;
    private final Region region;
    private final MineLocations locations;
    private final EditSession editSession;
    private final ProtectedRegion wgRegion;
    private final UUID npcId;
    private boolean open;
    private Material block;
    private BukkitTask task;
    private double taxPercentage;

    public PrivateMine(UUID owner, boolean open, Material block, Region region, MineLocations locations,
                       ProtectedRegion wgRegion, UUID npc, double taxPercentage) {
        this.owner = owner;
        this.open = open;
        this.region = region;
        this.locations = locations;
        this.editSession =
                new EditSessionBuilder(Objects.requireNonNull(region.getWorld()))
                        .allowedRegions(new Region[]{region}).build();
        this.block = block;
        this.wgRegion = wgRegion;
        this.npcId = npc;
        this.taxPercentage = taxPercentage;
        this.fill(block);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract("_ -> new")
    public static PrivateMine deserialize(Map<String, Object> map) {
        UUID owner = UUID.fromString((String) map.get("Owner"));
        boolean open = (Boolean) map.get("Open");
        Material block = Material.matchMaterial((String) map.get("Block"));
        Vector corner1 = Util.deserializeWorldEditVector((Map<String, Object>) map.get("Corner1"));
        Vector corner2 = Util.deserializeWorldEditVector(((Map<String, Object>) map.get("Corner2")));
        MineLocations locations = MineLocations.deserialize((Map<String, Object>) map.get("Locations"));
        CuboidRegion region = new CuboidRegion(corner1, corner2);
        region.setWorld(new BukkitWorld(locations.getSpawnPoint().getWorld()));
        ProtectedRegion wgRegion =
                WorldGuardPlugin.inst().getRegionManager(locations.getSpawnPoint().getWorld())
                        .getRegion(owner.toString());

        UUID npcId = UUID.fromString((String) map.get("NPC"));
        double taxPercentage = (Double) map.get("Tax");

        return new PrivateMine(owner, open, block, region, locations, wgRegion, npcId, taxPercentage);
    }

    public double getTaxPercentage() {
        return this.taxPercentage;
    }

    public void setTaxPercentage(double amount) {
        this.taxPercentage = amount;
    }

    public boolean contains(Player p) {
        return this.region.contains(Util.toVector(p.getLocation().toVector()));
    }

    public Material getBlock() {
        return block;
    }

    public void setBlock(Material block) {
        if (block.isBlock()) {
            this.block = block;
            this.fill(block);
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();
        map.put("Owner", this.owner.toString());
        map.put("Open", this.open);
        map.put("Block", this.block.name());
        map.put("Locations", this.locations.serialize());
        map.put("Corner1", Util.toVector(this.region.getMinimumPoint()).serialize());
        map.put("Corner2", Util.toVector(this.region.getMaximumPoint()).serialize());
        map.put("NPC", this.npcId.toString());
        map.put("Tax", this.taxPercentage);
        return map;
    }

    public void teleport(Player p) {
        p.teleport(locations.getSpawnPoint());
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

    public void fill(Material m) {
        //free any players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (locations.getRegion().contains(Util.toVector(player.getLocation().toVector()))) {
                player.teleport(locations.getSpawnPoint());
            }
        }

        //noinspection deprecation
        editSession.setBlocks(locations.getRegion(), new BaseBlock(m.getId()));
        editSession.flushQueue();

        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskLater(PrivateMines.getPlugin(PrivateMines.class),
                () -> fill(block), 1200L);
    }

    public void delete() {
        if (task != null) {
            task.cancel();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (contains(player)) {
                player.performCommand("spawn");
            }
        }

        //noinspection deprecation
        editSession.setBlocks(region, new BaseBlock(0));
        editSession.flushQueue();

        World world = locations.getSpawnPoint().getWorld();
        RegionManager regionManager =
                WorldGuardPlugin.inst().getRegionManager(world);

        if (regionManager == null) {
            PrivateMines.getPlugin(PrivateMines.class).getLogger()
                    .warning(String.format("RegionManager for world %s is null", world.getName()));
        } else {
            regionManager.removeRegion(wgRegion.getId());
            regionManager.removeRegion(locations.getWgRegion().getId());
        }

        NPC npc = CitizensAPI.getNPCRegistry().getByUniqueId(npcId);
        if (npc != null)
            npc.destroy();
    }
}
