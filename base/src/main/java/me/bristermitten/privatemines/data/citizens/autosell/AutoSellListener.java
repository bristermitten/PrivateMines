package me.bristermitten.privatemines.data.citizens.autosell;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.clip.autosell.events.AutoSellEvent;
import me.clip.autosell.events.SellAllEvent;
import me.clip.autosell.events.SignSellEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class AutoSellListener implements Listener {

    private final MineStorage storage;
    private final PMConfig config;

    public AutoSellListener(MineStorage storage, PMConfig config) {
        this.storage = storage;
        this.config = config;
    }

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent e) {
        if (!e.getNPC().hasTrait(AutoSellNPCTrait.class)) {
            return;
        }
        e.getClicker().performCommand(config.getSellCommand());
    }

    /*
      Used when a player right clicks the npc
     */

    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (!e.getNPC().hasTrait(AutoSellNPCTrait.class)) {
            return;
        }
        e.getClicker().performCommand(config.getSellCommand());
    }

    private void processTax(PrivateMine privateMine, double tax, Player player) {
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


    @EventHandler
    public void onAutoSell(AutoSellEvent e) {
        PrivateMine privateMine = storage.get(e.getPlayer());

        if (eventIsInvalid(Collections.singleton(e.getItemStackSold()), e.getPlayer())) {
            return;
        }

        if (privateMine != null && privateMine.hasTax()) {
            e.setMultiplier(1.0D - privateMine.getTaxPercentage() / 100.0D);
            double tax = e.getPrice() / 100.0D * privateMine.getTaxPercentage();
            processTax(privateMine, tax, e.getPlayer());
        }
    }

    @EventHandler
    public void onSellAll(SellAllEvent e) {
        if (eventIsInvalid(e.getItemsSold(), e.getPlayer())) {
            return;
        }

        final PrivateMine privateMine = storage.get(e.getPlayer().getUniqueId());

        if (privateMine != null && privateMine.hasTax()) {
            double tax = e.getTotalCost() / 100.0 * privateMine.getTaxPercentage();
            e.setTotalCost(e.getTotalCost() - tax);
            processTax(privateMine, tax, e.getPlayer());
        }
    }

    private boolean eventIsInvalid(Collection<ItemStack> itemsSold, Player player) {
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

    @EventHandler
    public void onSignSell(SignSellEvent e) {
        if (eventIsInvalid(e.getItemsSold(), e.getPlayer())) {
            return;
        }
        PrivateMine privateMine = storage.get(e.getPlayer());

        if (privateMine != null && privateMine.hasTax()) {
            double tax = e.getTotalCost() / 100.0 * privateMine.getTaxPercentage();
            e.setTotalCost(e.getTotalCost() - tax);
            processTax(privateMine, tax, e.getPlayer());
        }
    }
}
