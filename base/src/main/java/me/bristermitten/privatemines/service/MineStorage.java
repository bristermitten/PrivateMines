package me.bristermitten.privatemines.service;

import com.google.common.collect.ImmutableSet;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MineStorage {

    public static final String DATA_DO_NOT_CHANGE = "Data-Do-Not-Change";
    private final Map<UUID, PrivateMine> mines = new HashMap<>();
    private final MineFactory<MineSchematic<?>, ?> factory;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public MineStorage(MineFactory<MineSchematic<?>, ?> factory) {
        this.factory = factory;
    }

    /**
     * Save the stored mines to a given {@link YamlConfiguration}
     *
     * @param configuration The configuration to save to
     */
    public void save(YamlConfiguration configuration) {
        configuration.set(DATA_DO_NOT_CHANGE, factory.getManager().serialize());
        configuration.set("Mines", mines.values().stream().map(PrivateMine::serialize).collect(Collectors.toList()));
    }

    /**
     * Load private mines from a given {@link YamlConfiguration}
     * This is typically only called internally, as it will fail if the format does not match that created by {@link MineStorage#save(YamlConfiguration)}
     *
     * @param config The configuration to load from
     */
    @SuppressWarnings("unchecked")
    public void load(YamlConfiguration config) {
        final ConfigurationSection dataSection = config.getConfigurationSection(DATA_DO_NOT_CHANGE);

        if (dataSection != null) {
            factory.getManager().load(dataSection.getValues(true));
        }

        for (Map<?, ?> map : config.getMapList("Mines")) {
            this.load(PrivateMine.deserialize((Map<String, Object>) map));
        }

    }


    private void load(PrivateMine mine) {
        mines.put(mine.getOwner(), mine);
    }

    /**
     * Get a Player's private mine, or create a new one if they do not have one
     *
     * @param player The player to get or create a private mine for
     * @return a private mine object, that may be newly created for the player
     * @throws IllegalStateException if there is no default schematic configured in {@link SchematicStorage}
     */
    @NotNull
    public PrivateMine getOrCreate(@NotNull Player player) {
        PrivateMine mine = mines.get(player.getUniqueId());

        if (mine != null) {
            return mine;
        }

        MineSchematic<?> defaultSchematic = SchematicStorage.getInstance().getDefaultSchematic();
        if (defaultSchematic == null) {
            player.sendMessage(ChatColor.RED + "No Default Schematic. Contact an Admin.");
            logger.severe("There was no default schematic, to fix this error,");
            logger.severe("go into the schematics.yml file and add a new schematic");
            logger.severe("and make sure that you have at least one schematics default");
            logger.severe("option set to true.");
            throw new IllegalStateException("No Default Schematic found"); //TODO this needs to be more user friendly
        }

        mine = factory.create(player, defaultSchematic, true);
        mines.put(player.getUniqueId(), mine);

        return mine;
    }

    /**
     * Get an OfflinePlayer's private mine
     *
     * @param player The player to get the mine of
     * @return The player's private mine, or null if they do not have one
     */
    @Nullable
    public PrivateMine get(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    /**
     * Get a player's private mine via their UUID
     *
     * @param uuid The player's UUID to get the mine of
     * @return The player's private mine, or null if they do not have one
     */
    @Nullable
    public PrivateMine get(UUID uuid) {
        return mines.get(uuid);
    }


    /**
     * Check if a given {@link OfflinePlayer} has a private mine
     *
     * @param player the player to check the presence of a mine
     * @return if the player has a private mine
     */
    public boolean hasMine(OfflinePlayer player) {
        return mines.containsKey(player.getUniqueId());
    }

    /**
     * Remove a player's private mine
     * If the player does not have a mine, this will do nothing.
     *
     * @param target The player to remove the mine of
     * @apiNote This method will not actually delete the mine schematic, remove players, kill NPCs or any other operations. It simply removes a mine from the storage, as such invalidating it from many operations.
     */
    public void remove(OfflinePlayer target) {
        mines.remove(target.getUniqueId());
    }

    /**
     * Get all private mines.
     *
     * @return an immutable collection of all private mines in the storage.
     */
    @Unmodifiable
    public Set<PrivateMine> getAll() {
        return ImmutableSet.copyOf(mines.values());
    }
}
