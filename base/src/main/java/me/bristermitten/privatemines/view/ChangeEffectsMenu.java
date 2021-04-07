package me.bristermitten.privatemines.view;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.config.menu.MenuSpec;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.List;
import java.util.stream.Collectors;

public class ChangeEffectsMenu {

    private static MenuSpec original;

    /*
        Loads the configuration for the change effects menu
     */

    public ChangeEffectsMenu(Player p, PrivateMines plugin, PMConfig config, MenuConfig menuConfig, MineStorage storage) {
        if (original == null) {
            original = new MenuSpec();
            original.loadFrom(menuConfig.configurationForName("Effects"));
        }

        MenuSpec spec = new MenuSpec();
        spec.copyFrom(original);
        spec.register(plugin);
        PrivateMine mine = storage.getOrCreate(p);
        ParticleEffect[] effects = config.getParticleEffects().toArray(new ParticleEffect[]{});
        p.openInventory(spec.genMenu((effect, i) -> {
                    i.setType(Material.REDSTONE);
                    ItemMeta itemMeta = i.getItemMeta();
                    String displayName = itemMeta.getDisplayName();
                    String name = displayName.replace("%effect%",
                            effect.toString());

                    itemMeta.setDisplayName(name);
                    List<String> lore = itemMeta.getLore().stream().map(s -> s.replace("%effect%",
                            effect.toString())).collect(Collectors.toList());
                    itemMeta.setLore(lore);
                    i.setItemMeta(itemMeta);
                    return i;
                },
                effect -> e -> {
                    if (p.hasPermission("privatemine.effect." + effect.name())) {
                        p.sendMessage("Changed effect to " + effect);
                    } else {
                        p.sendMessage(ChatColor.RED + "No access to this effect!");
                    }
                }, effects));
    }
}
