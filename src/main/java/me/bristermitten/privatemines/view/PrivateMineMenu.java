package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.entity.Player;

public class PrivateMineMenu {

    private final Player p;

    private final MineStorage storage;

    public PrivateMineMenu(Player p, PrivateMines plugin, MenuConfig config, MineStorage storage) {
        this.p = p;
        this.storage = storage;


        MenuSpec menuSpec = new MenuSpec();
        menuSpec.load(config.forName("Mines"));
        menuSpec.putAction("go-to-mine", e -> {
            storage.getOrCreate((Player) e.getWhoClicked()).teleport();
        });
        menuSpec.genMenu();
        menuSpec.register(plugin);
        p.openInventory(menuSpec.getMenu());
    }
}
