package me.bristermitten.privatemines.service;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class MineStorage {

    private final Map<UUID, PrivateMine> mines = new HashMap<>();

    private final MineFactory factory;

    public MineStorage(MineFactory factory) {
        this.factory = factory;
    }

    public void save(YamlConfiguration configuration) {
        configuration.set("Mines", mines.values().stream().map(PrivateMine::serialize).collect(toList()));
    }

    public void load(YamlConfiguration configuration) {
        for (Map<?, ?> key : configuration.getMapList("Mines")) {
            load(PrivateMine.deserialize((Map<String, Object>) key));
        }
    }

    private void load(PrivateMine mine) {
        mines.put(mine.getOwner(), mine);
        System.out.println("Loaded mine for " + mine.getOwner());
    }

    public void put(Player p, PrivateMine mine) {
        mines.put(p.getUniqueId(), mine);
    }

    public PrivateMine getOrCreate(Player p) {
        PrivateMine mine = mines.get(p.getUniqueId());

        if (mine == null) {
            mine = factory.create(p);
            mines.put(p.getUniqueId(), mine);
        }

        return mine;
    }

    public boolean has(Player p) {
        return mines.containsKey(p);
    }
}
