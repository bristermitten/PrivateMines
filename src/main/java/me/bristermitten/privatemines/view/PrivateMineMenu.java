package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;

public class PrivateMineMenu {

    private static MenuSpec original;

    public PrivateMineMenu(Player p, PrivateMines plugin, MenuConfig config, MineStorage storage, PMConfig pmConfig, File f) {
        Validate.notNull(p, "Player");
        Validate.notNull(plugin, "PrivateMines");
        Validate.notNull(config, "MenuConfig");
        Validate.notNull(storage, "MineStorage");
        Validate.notNull(pmConfig, "PMConfig");

        if (original == null) {
            original = new MenuSpec();

            original.addAction("go-to-mine",
                    e -> goToMine(storage, e, f));
            original.addAction("view-mines",
                    e -> openMinesMenu(plugin, config, storage, f, e));
            original.addAction("change-block",
                    e -> openChangeBlockMenu(plugin, config, storage, pmConfig, e, f));
        }
        PrivateMine mine = storage.get(p);
        Material type = mine != null ? mine.getBlock() : Material.STONE;

        original.loadFrom(config.configurationForName("Main"), "%BLOCK%", type.name());

        MenuSpec menuSpec = new MenuSpec();

        menuSpec.copyFrom(original);
        menuSpec.register(plugin);

        p.openInventory(menuSpec.genMenu());
    }

    private void openMinesMenu(PrivateMines plugin, MenuConfig config, MineStorage storage, File f, PMConfig pmConfig, InventoryClickEvent e) {
    }

    private void goToMine(MineStorage storage, InventoryClickEvent e, File f) {
        storage.getOrCreate((Player) e.getWhoClicked()).teleport();
    }

    private void openMinesMenu(PrivateMines plugin, MenuConfig config, MineStorage storage, File f, InventoryClickEvent e) {
        new MinesMenu(config, plugin, storage, f).open((Player) e.getWhoClicked());
    }

    private void openChangeBlockMenu(PrivateMines plugin, MenuConfig config, MineStorage storage, PMConfig pmConfig,
                                     InventoryClickEvent e, File f) {
        new ChangeBlockMenu((Player) e.getWhoClicked(), plugin, pmConfig,
                config, storage, f);
    }
}
