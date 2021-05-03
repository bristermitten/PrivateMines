package me.bristermitten.privatemines.data.citizens.autosell;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
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

import java.util.List;
import java.util.UUID;

public class AutoSellListener implements Listener {

    private final MineStorage storage = PrivateMines.getPlugin().getStorage();

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent e) {
        if (!e.getNPC().hasTrait(AutoSellNPCTrait.class))
        {
            return;
        }
        e.getClicker().performCommand("sellall");
        //TODO Possibly add a price gui?
    }

    /*
      Used when a player right clicks the npc
     */

    @EventHandler
    public void onRightClick(NPCRightClickEvent e)
    {
        if (!e.getNPC().hasTrait(AutoSellNPCTrait.class))
        {
            return;
        }
        e.getClicker().performCommand("sellall");
    }

    private void process(PrivateMine privateMine, double tax, Player player)
    {
        UUID owner = privateMine.getOwner();
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);

        BukkitCommandManager manager = PrivateMines.getPlugin().getManager();
        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);
        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(tax),
                "{tax}", String.valueOf(privateMine.getTaxPercentage()));
        if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(owner))) {
            BukkitCommandIssuer ownerIssuer = manager.getCommandIssuer(Bukkit.getPlayer(owner));

            manager.sendMessage(ownerIssuer, MessageType.INFO, LangKeys.INFO_TAX_RECIEVED,
                    "{amount}", String.valueOf(tax),
                    "{tax}", String.valueOf(privateMine.getTax()));
        }
    }

    /*
        AutoSell Sell System Listeners..
    */

    @EventHandler
    public void onAutoSell(AutoSellEvent e)
    {
        //No need to tax the owner

        PrivateMine privateMine = storage.get(e.getPlayer());

        if (privateMine == null)
        {
            return;
        }

        if (!privateMine.contains(e.getPlayer()))
        {
            return;
        }

        e.setMultiplier(1.0D - privateMine.getTaxPercentage() / 100.0D);
        double tax = e.getPrice() / 100.0D * privateMine.getTaxPercentage();
        process(privateMine, tax, e.getPlayer());
    }

    @EventHandler
    public void onSellAll(SellAllEvent e)
    {
        if (eventIsNotApplicable(e.getItemsSold(), e.getPlayer()))
        {
            return;
        }
        PrivateMine privateMine = storage.get(e.getPlayer().getUniqueId());

        double tax = e.getTotalCost() / 100.0 * privateMine.getTaxPercentage();
        e.setTotalCost(e.getTotalCost() - tax);
        process(privateMine, tax, e.getPlayer());
    }

    private boolean eventIsNotApplicable(List<ItemStack> itemsSold, Player player)
    {
        if (itemsSold.isEmpty())
        {
            return true;
        }

        PrivateMine privateMine = storage.get(player);
        if (privateMine == null)
        {
            return true;
        }
        return !privateMine.contains(player);
    }

    @EventHandler
    public void onSignSell(SignSellEvent e)
    {
        if (eventIsNotApplicable(e.getItemsSold(), e.getPlayer()))
        {
            return;
        }
        PrivateMine privateMine = storage.get(e.getPlayer());
        double tax = (e.getTotalCost() / 100.0) * privateMine.getTaxPercentage();
        e.setTotalCost(e.getTotalCost() - tax);
        process(privateMine, tax, e.getPlayer());
    }
}