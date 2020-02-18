package me.UntouchedOdin0.privatemines.view;

import me.UntouchedOdin0.privatemines.PrivateMines;
import me.UntouchedOdin0.privatemines.config.PMConfig;
import me.UntouchedOdin0.privatemines.config.menu.MenuConfig;
import me.UntouchedOdin0.privatemines.service.MineStorage;

import org.bukkit.entity.Player;

public class MenuFactory {
    private final MineStorage storage;

    private final PrivateMines plugin;
    private final MenuConfig config;
    private final PMConfig pmConfig;

    public MenuFactory(MineStorage storage, PrivateMines plugin, MenuConfig config, PMConfig pmConfig) {
        this.storage = storage;
        this.plugin = plugin;
        this.config = config;
        this.pmConfig = pmConfig;
    }

    public void createMenu(Player p) {
        new me.UntouchedOdin0.privatemines.view.PrivateMineMenu(p, plugin, config, storage, pmConfig);
    }

    public void createMinesMenu(Player p) {
        new MinesMenu(p, config, plugin, storage);
    }
}
