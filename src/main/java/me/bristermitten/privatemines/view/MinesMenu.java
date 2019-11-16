package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;

public class MinesMenu {
    private static MenuSpec original;

    public MinesMenu(Player p, MenuConfig config, PrivateMines plugin, MineStorage storage) {

        if (original == null) {
            original = new MenuSpec();
//            original.putAction("go-to-mine", e -> storage.getOrCreate((Player) e.getWhoClicked()).teleport());
//            original.putAction("view-mines", e -> new MinesMenu(p));
            original.load(config.forName("Mines"));

        }

        MenuSpec spec = new MenuSpec();
        spec.load(original);
        spec.register(plugin);
        Player[] players =
                Bukkit.getOnlinePlayers().stream().filter(storage::has).filter(pl -> storage.get(pl).isOpen())
                        .toArray(Player[]::new);
        Inventory inventory = spec.genMenu(
                (pl, i) -> {
                    ItemMeta itemMeta = i.getItemMeta();
                    String name = itemMeta.getDisplayName().replace("%player%", pl.getName());
                    itemMeta.setDisplayName(name);
                    i.setItemMeta(itemMeta);
                    return i;
                },
                pl -> e -> storage.getOrCreate(pl).teleport((Player) e.getWhoClicked()), players);
        spec.register(plugin);
        p.openInventory(inventory);
    }
}
