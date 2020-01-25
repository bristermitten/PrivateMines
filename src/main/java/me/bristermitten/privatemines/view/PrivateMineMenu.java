package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.service.MineStorage;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PrivateMineMenu {

    private static MenuSpec original;

    public PrivateMineMenu(Player p, PrivateMines plugin, MenuConfig config, MineStorage storage, PMConfig pmConfig) {
        Validate.notNull(p, "Player");
        Validate.notNull(plugin, "PrivateMines");
        Validate.notNull(config, "MenuConfig");
        Validate.notNull(storage, "MineStorage");
        Validate.notNull(pmConfig, "PMConfig");

        if (original == null) {
            original = new MenuSpec();
            original.putAction("go-to-mine", e -> storage.getOrCreate((Player) e.getWhoClicked()).teleport());
            original.putAction("view-mines", e -> new MinesMenu((Player) e.getWhoClicked(), config, plugin, storage));
            original.putAction("change-block", e -> new ChangeBlockMenu((Player) e.getWhoClicked(), plugin, pmConfig,
                    config, storage));
        }
        Material type = storage.has(p) ? storage.get(p).getBlock() : Material.STONE;

        original.loadFrom(config.configurationForName("Main"), "%BLOCK%", type.name());

        MenuSpec menuSpec = new MenuSpec();

        menuSpec.copyFrom(original);
        menuSpec.register(plugin);

        p.openInventory(menuSpec.genMenu());
    }
}
