package me.bristermitten.privatemines.data;

import com.boydti.fawe.util.EditSessionBuilder;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PrivateMine implements ConfigurationSerializable {
    private UUID owner;
    private boolean open;
    private Region region;
    private MineLocations locations;

    private EditSession editSession;

    public PrivateMine(UUID owner, boolean open, Region region, MineLocations locations) {
        this.owner = owner;
        this.open = open;
        this.region = region;
        this.locations = locations;
        this.editSession =
                new EditSessionBuilder(Objects.requireNonNull(region.getWorld())).allowedRegions(new Region[]{locations.getRegion()})
                        .build();

        //TODO REMOVE
        fill(Material.DIAMOND_BLOCK);

    }

    public static PrivateMine deserialize(Map<String, Object> map) {
        UUID owner = UUID.fromString((String) map.get("Owner"));
        boolean open = (boolean) map.get("Open");
        BlockVector corner1 = (BlockVector) map.get("Corner1");
        BlockVector corner2 = (BlockVector) map.get("Corner2");
        MineLocations locations = (MineLocations) map.get("Locations");
        return new PrivateMine(owner, open, new CuboidRegion(corner1, corner2), locations);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Owner", owner.toString());
        map.put("Open", open);
        map.put("Corner1", region.getMinimumPoint().toBlockPoint());
        map.put("Corner2", region.getMaximumPoint().toBlockPoint());
        map.put("Locations", locations);
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

    public void fill(Material m) {
        editSession.setBlocks(locations.getRegion(), new BaseBlock(m.getId()));
        editSession.flushQueue();
    }
}
