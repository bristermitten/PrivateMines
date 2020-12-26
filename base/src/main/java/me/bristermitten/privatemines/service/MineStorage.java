package me.bristermitten.privatemines.service;

import com.google.common.collect.ImmutableSet;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MineStorage {

    public static final String DATA_DO_NOT_CHANGE = "Data-Do-Not-Change";
    private final Map<UUID, PrivateMine> mines = new HashMap<>();
    private final MineFactory factory;

    public MineStorage(MineFactory factory) {
        this.factory = factory;
    }

    /*
      Saves the yml file for mine storage.
     */
    public void save(YamlConfiguration configuration) {
        configuration.set(DATA_DO_NOT_CHANGE, factory.getManager().serialize());
        configuration.set("Mines", mines.values().stream().map(PrivateMine::serialize).collect(Collectors.toList()));
    }

    /*
      Loads the yml file for mine storage.
     */
    @SuppressWarnings("unchecked")
    public void load(YamlConfiguration config) {
        if (config.contains(DATA_DO_NOT_CHANGE)) {
            factory.getManager().load(config.getConfigurationSection(DATA_DO_NOT_CHANGE)
                    .getValues(true));
        }

        for (Map<?, ?> map : config.getMapList("Mines")) {
            this.load(PrivateMine.deserialize((Map<String, Object>) map));
        }

    }

    /*
     Loads the private mine and sets the owner to the mine.
     */
    private void load(PrivateMine mine) {
        mines.put(mine.getOwner(), mine);
    }

    /*
     Either gets the private mine for the player or it creates a new one
     */
    public PrivateMine getOrCreate(Player p) {
        PrivateMine mine = mines.get(p.getUniqueId());

        if (mine == null) {

            MineSchematic<?> defaultSchematic = SchematicStorage.getInstance().getDefaultSchematic();
            if (defaultSchematic == null) {
                p.sendMessage(ChatColor.RED + "No Default Schematic. Contact an Admin.");
                throw new IllegalStateException("No Default Schematic found");
            }

            mine = factory.create(p, defaultSchematic);
            mines.put(p.getUniqueId(), mine);
        }

        return mine;
    }

    /*
      Gets the player's private mine
     */
    public PrivateMine get(Player p) {
        return mines.get(p.getUniqueId());
    }

    /*
      Gets the players private mine
     */
    public PrivateMine get(UUID p) {
        return mines.get(p);
    }

    /*
      Checks if the player has a private mine.
     */
    public boolean has(Player p) {
        return mines.containsKey(p.getUniqueId());
    }

    /*
     Removes a player's private mine.
     */
    public void remove(Player target) {
        mines.remove(target.getUniqueId());
    }

    public Set<PrivateMine> getAll() {
        return ImmutableSet.copyOf(mines.values());
    }
}
