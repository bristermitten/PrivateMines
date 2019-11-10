package me.bristermitten.privatemines.service;

import me.bristermitten.privatemines.data.PrivateMine;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MineStorage {

    private final Map<Player, PrivateMine> mines = new HashMap<>();

    public void save() {

    }

    public void put(Player p, PrivateMine mine) {
        mines.put(p, mine);
    }

    public boolean has(Player p) {
        return mines.containsKey(p);
    }
}
