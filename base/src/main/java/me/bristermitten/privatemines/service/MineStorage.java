package me.bristermitten.privatemines.service;

import com.google.common.collect.ImmutableSet;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class MineStorage {

    public static final String DATA_DO_NOT_CHANGE = "Data-Do-Not-Change";
    private final Map<UUID, PrivateMine> mines = new HashMap<>();
    private final MineFactory<MineSchematic<?>, ?> factory;

    public MineStorage(MineFactory<MineSchematic<?>, ?> factory) {
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
            factory.getManager().load(Objects.requireNonNull(config.getConfigurationSection(DATA_DO_NOT_CHANGE))
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

        if (mine != null) {
            return mine;
        }

        MineSchematic<?> defaultSchematic = SchematicStorage.getInstance().getDefaultSchematic();
        if (defaultSchematic == null) {
            p.sendMessage(ChatColor.RED + "No Default Schematic. Contact an Admin.");
            throw new IllegalStateException("No Default Schematic found"); //TODO this needs to be more user friendly
        }

        mine = factory.create(p, defaultSchematic, true);
        mines.put(p.getUniqueId(), mine);

        return mine;
    }

    /*
      Gets the player's private mine
     */
    public PrivateMine get(OfflinePlayer p) {
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
    public boolean has(OfflinePlayer p) {
        return mines.containsKey(p.getUniqueId());
    }

    /*
     Removes a player's private mine.
     */
    public void remove(OfflinePlayer target) {
        mines.remove(target.getUniqueId());
    }

    public Set<PrivateMine> getAll() {
        return ImmutableSet.copyOf(mines.values());
    }
}
