package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChangeResetStyleMenu {

    private static MenuSpec original;

    /*
        Loads the configuration for the change effects menu
     */

    ChangeResetStyleMenu(Player p, PrivateMines plugin, PMConfig config, MenuConfig menuConfig, MineStorage storage) {
        if (original == null) {
            original = new MenuSpec();
            original.loadFrom(menuConfig.configurationForName("Styles"));
        }

        MenuSpec spec = new MenuSpec();
        spec.copyFrom(original);
        spec.register(plugin);
        PrivateMine mine = storage.get(p);
        String[] resetStyles = config.getResetStyles().toArray(new String[]{});

        p.openInventory(spec.genMenu((style, i) -> {
                    i.setType(Material.REDSTONE);
                    ItemMeta itemMeta = i.getItemMeta();
                    if (itemMeta != null && itemMeta.hasDisplayName()) {
                        String displayName = itemMeta.getDisplayName();
                        String name = displayName.replace("%style%",
                                Util.parseStyle(style));
                        itemMeta.setDisplayName(name);
                        if (itemMeta.hasLore()) {
                            List<String> lore = Objects.requireNonNull(itemMeta.getLore()).stream().map(s -> s.replace("%style%",
                                    Util.parseStyle(style))).collect(Collectors.toList());
                            itemMeta.setLore(lore);
                        }
                    }
                    i.setItemMeta(itemMeta);
                    return i;
                },
                style -> e -> {
                    if (p.hasPermission("privatemine.style." + style)) {
                        p.sendMessage(ChatColor.GREEN + "Changed reset style to to " + Util.parseStyle(style));
                        if (mine != null) {
                            mine.setResetStyle(style);
                        }
                        p.closeInventory();
                    } else {
                        p.sendMessage(ChatColor.RED + "No access to this style!");
                    }
                }, resetStyles));
    }
}