//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.service;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MineStorage {
    private final Map<UUID, PrivateMine> mines = new HashMap<>();
    private final MineFactory factory;

    public MineStorage(MineFactory factory) {
        this.factory = factory;
    }

    public void save(YamlConfiguration configuration) {
        configuration.set("Data-Do-Not-Change", factory.getManager().serialize());
        configuration.set("Mines", mines.values().stream().map(PrivateMine::serialize).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public void load(YamlConfiguration config) {
        if (config.contains("Data-Do-Not-Change")) {
            factory.getManager().load(config.getConfigurationSection("Data-Do-Not-Change")
                    .getValues(true));
        }

        for (Map<?, ?> map : config.getMapList("Mines")) {
            this.load(PrivateMine.deserialize((Map<String, Object>) map));
        }

    }

    private void load(PrivateMine mine) {
        mines.put(mine.getOwner(), mine);
    }

    @Nonnull
    public PrivateMine getOrCreate(Player p, File f) {
        PrivateMine mine = mines.get(p.getUniqueId());

        if (mine == null) {
            mine = factory.create(p, f);
            mines.put(p.getUniqueId(), mine);
        }

        return mine;
    }

    public PrivateMine get(Player p) {
        return mines.get(p.getUniqueId());
    }

    public PrivateMine get(UUID p) {
        return mines.get(p);
    }

    public boolean has(Player p) {
        return mines.containsKey(p.getUniqueId());
    }

    public void remove(Player target) {
        mines.remove(target.getUniqueId());
    }
}
