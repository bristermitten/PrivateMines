package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.entity.Player;

public class MenuFactory {
    private final MineStorage storage;

    private final PrivateMines plugin;
    private final MenuConfig config;

    public MenuFactory(MineStorage storage, PrivateMines plugin, MenuConfig config) {
        this.storage = storage;
        this.plugin = plugin;
        this.config = config;
    }

    public PrivateMineMenu createMenu(Player p) {
        return new PrivateMineMenu(p, plugin, config, storage);
    }
}
