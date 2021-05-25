package me.bristermitten.privatemines.data.citizens.ultraprisoncore;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.drawethree.ultraprisoncore.api.events.UltraPrisonSellAllEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class UltraPrisonListener implements Listener {
    private final MineStorage storage = PrivateMines.getPlugin().getStorage();
    private static final BukkitCommandManager manager = PrivateMines.getPlugin().getManager();

    PrivateMine privateMine;

    UUID owner;

    private Double tax;

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent e) {
        if (!e.getNPC().hasTrait(UltraPrisonCoreNPCTrait.class))
            return;
        e.getClicker().performCommand("sellall");
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (!e.getNPC().hasTrait(UltraPrisonCoreNPCTrait.class))
            return;
        e.getClicker().performCommand("sellall");
    }

    private boolean eventIsNotApplicable(Player player) {
        privateMine = storage.get(player.getUniqueId());
        if (privateMine != null) {
            owner = privateMine.getOwner();
            return !privateMine.contains(player);
        }

        return owner != null && !player.getUniqueId().equals(owner);
    }

    private void process(PrivateMine privateMine, double tax, Player player) {
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);

        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);
        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(tax),
                "{tax}", String.valueOf(privateMine.getTaxPercentage()));
        if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(owner))) {
            BukkitCommandIssuer ownerIssuer = manager.getCommandIssuer(Bukkit.getPlayer(owner));

            manager.sendMessage(ownerIssuer, MessageType.INFO, LangKeys.INFO_TAX_RECEIVED,
                    "{amount}", String.valueOf(tax),
                    "{tax}", String.valueOf(privateMine.getTax()));
        }
    }

    @EventHandler
    public void onUPCSellAllEvent(UltraPrisonSellAllEvent e) {
        Player player = e.getPlayer();
        privateMine = storage.get(player.getUniqueId());
        if (privateMine != null) {
            owner = privateMine.getOwner();

            if (owner == null)
                Bukkit.getLogger().warning("UltraPrisonSellAllEvent owner was null!");
            if (eventIsNotApplicable(player))
                return;
            try {
                double defaultTax = PrivateMines.getPlugin().getConfig().getDouble("Tax-Percentage");
                if (privateMine.getTaxPercentage() > 0.0D) {
                    tax = privateMine.getTaxPercentage();
                    if (tax == 0.0D)
                        tax = defaultTax;
                } else if (privateMine.getTaxPercentage() == 0.0D) {
                    return;
                }
                double d = e.getSellPrice();
                if (tax == 0.0D)
                    tax = defaultTax;
                double takenTax = d / 100.0D * tax;
                double afterTax = d - takenTax;

                if (afterTax == 0) {
                    player.sendMessage(ChatColor.RED + "You made no money from this sale!");
                    return;
                }

                e.setSellPrice(afterTax);
                process(privateMine, takenTax, player);
            } catch (NullPointerException e1) {
                e1.printStackTrace();
            }
        }
    }
}