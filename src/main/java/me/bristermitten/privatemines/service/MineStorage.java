package me.bristermitten.privatemines.service;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MineStorage {

    private final Map<Player, PrivateMine> mines = new HashMap<>();

    private final MineFactory factory;

    public MineStorage(MineFactory factory) {
        this.factory = factory;
    }

    public void save() {

    }

    public void put(Player p, PrivateMine mine) {
        mines.put(p, mine);
    }

    public PrivateMine getOrCreate(Player p) {
        PrivateMine mine = mines.get(p);

        if (mine == null) {
            mine = factory.create(p);
            mines.put(p, mine);
        }

        return mine;
    }

    public boolean has(Player p) {
        return mines.containsKey(p);
    }
}
