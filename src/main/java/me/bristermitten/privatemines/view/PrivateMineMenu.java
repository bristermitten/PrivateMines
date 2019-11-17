package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PrivateMineMenu {

    private static MenuSpec original;

    public PrivateMineMenu(Player p, PrivateMines plugin, MenuConfig config, MineStorage storage, PMConfig pmConfig) {
        if (original == null) {
            original = new MenuSpec();
            original.putAction("go-to-mine", e -> storage.getOrCreate((Player) e.getWhoClicked()).teleport());
            original.putAction("view-mines", e -> new MinesMenu((Player) e.getWhoClicked(), config, plugin, storage));
            original.putAction("change-block", e -> new ChangeBlockMenu((Player) e.getWhoClicked(), plugin, pmConfig,
                    config, storage));
        }
        original.load(config.forName("Main"), "%BLOCK%", (storage.has(p) ? storage.get(p).getBlock().name() :
                Material.STONE.name()));

        MenuSpec menuSpec = new MenuSpec();
        menuSpec.load(original);
        menuSpec.register(plugin);
        p.openInventory(menuSpec.genMenu());
    }
}
