package me.bristermitten.privatemines.data.citizens;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.UUID;

public class Taxation {
    private Taxation() {
    }

    public static boolean cannotBeTaxed(MineStorage storage, Collection<ItemStack> itemsSold, Player player) {
        if (itemsSold.isEmpty()) {
            return true;
        }

        PrivateMine privateMine = storage.get(player);
        if (privateMine == null) {
            return true;
        }
        if (privateMine.getOwner().equals(player.getUniqueId())) {
            //No need to tax the owner
            return true;
        }
        return !privateMine.contains(player);
    }


    public static void processTax(PrivateMine privateMine, double tax, Player player) {
        UUID owner = privateMine.getOwner();
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);

        BukkitCommandManager manager = PrivateMines.getPlugin().getManager();
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
}
