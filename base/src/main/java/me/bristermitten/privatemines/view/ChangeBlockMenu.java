package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Handles the menu to change a Private Mine block
 */
public class ChangeBlockMenu {

    private static MenuSpec original;

    /*
    Loads the configuration for the change block menu
     */

    ChangeBlockMenu(Player p, PrivateMines plugin, PMConfig config, MenuConfig menuConfig, MineStorage storage) {
        if (original == null) {
            original = new MenuSpec();
            original.loadFrom(menuConfig.configurationForName("Blocks"));
        }

        MenuSpec spec = new MenuSpec();
        spec.copyFrom(original);
        spec.register(plugin);
        PrivateMine mine = storage.get(p);
        ItemStack[] blocks = config.getBlockOptions().toArray(new ItemStack[]{});

        p.openInventory(spec.genMenu((block, i) -> {
                    i.setType(block.getType());
                    ItemMeta itemMeta = i.getItemMeta();
                    Damageable damageable = (Damageable) itemMeta;
                    if (damageable != null) {
                        damageable.setDamage(damageable.getDamage());
                    }

                    String displayName = null;
                    if (itemMeta != null) {
                        displayName = itemMeta.getDisplayName();
                    }
                    String name = null;
                    if (displayName != null) {
                        name = displayName.replace("%block%",
                                Util.getItemName(block).orElse("Unknown"));
                    }
                    if (itemMeta != null) {
                        itemMeta.setDisplayName(name);
                    }
                    List<String> lore = null;
                    if (itemMeta != null) {
                        lore = Objects.requireNonNull(itemMeta.getLore()).stream().map(s -> s.replace("%block%",
                                Util.getItemName(block).orElse("Unknown"))).collect(Collectors.toList());
                    }
                    if (itemMeta != null) {
                        itemMeta.setLore(lore);
                    }
                    i.setItemMeta(itemMeta);
                    return i;
                },
                block -> e -> {
                    if (p.hasPermission("privatemine.block." + block.getType().name())) {
                        if (mine != null) {
                            mine.fillWESingle(block);
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "No access to this block!");
                    }
                }, blocks));
    }
}
