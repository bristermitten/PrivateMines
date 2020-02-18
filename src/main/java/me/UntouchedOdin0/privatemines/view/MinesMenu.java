package me.UntouchedOdin0.privatemines.view;


import me.UntouchedOdin0.privatemines.PrivateMines;
import me.UntouchedOdin0.privatemines.Util;
import me.UntouchedOdin0.privatemines.config.menu.MenuConfig;
import me.UntouchedOdin0.privatemines.config.menu.MenuSpec;
import me.UntouchedOdin0.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

import static me.UntouchedOdin0.privatemines.Util.prettify;

public class MinesMenu {
    private static MenuSpec original;

    public MinesMenu(Player p, MenuConfig config, PrivateMines plugin, MineStorage storage) {

        if (original == null) {
            original = new MenuSpec();
            original.loadFrom(config.configurationForName("Mines"));
        }

        MenuSpec spec = new MenuSpec();
        spec.copyFrom(original);
        spec.register(plugin);

        Player[] players =
                Bukkit.getOnlinePlayers().stream().filter(storage::has).filter(pl -> storage.get(pl).isOpen())
                        .toArray(Player[]::new);

        Inventory inventory = spec.genMenu(
                (pl, i) -> {
                    ItemMeta itemMeta = i.getItemMeta();
                    Util.replaceMeta(itemMeta,
                            "%player%", pl.getName(),
                            "%block%", prettify(storage.getOrCreate(pl).getBlock().name()),
                            "%tax%", String.valueOf(storage.getOrCreate(pl).getTaxPercentage()));
                    i.setItemMeta(itemMeta);
                    return i;
                },
                pl -> e -> storage.getOrCreate(pl).teleport((Player) e.getWhoClicked()), players);

        spec.register(plugin);
        p.openInventory(inventory);
    }
}
