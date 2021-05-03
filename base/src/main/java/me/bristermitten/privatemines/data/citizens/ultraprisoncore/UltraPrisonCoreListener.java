package me.bristermitten.privatemines.data.citizens.ultraprisoncore;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.data.citizens.autosell.AutoSellNPCTrait;
import me.bristermitten.privatemines.service.MineStorage;
import me.drawethree.ultraprisoncore.api.events.UltraPrisonSellAllEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.Persist;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class UltraPrisonCoreListener implements Listener {

    private final MineStorage storage = PrivateMines.getPlugin().getStorage();
    PrivateMine privateMine;
    UUID owner = privateMine.getOwner();
    private Double tax;


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

    private boolean eventIsNotApplicable(Player player) {
        if (player.getUniqueId().equals(owner))
            return false;
        privateMine = storage.get(owner);
        if (privateMine == null)
            return true;
        return !privateMine.contains(player);
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
    public void onUPCSellAllEvent(UltraPrisonSellAllEvent e) {
        Player player = e.getPlayer();
        privateMine = storage.get(owner);
        double defaultTax = 0.0D;
        if (eventIsNotApplicable(player))
            return;
        try {
            if (privateMine.getTaxPercentage() > 0.0D) {
                try {
                    tax = privateMine.getTaxPercentage();
                } catch (NullPointerException exc) {
                    defaultTax = PrivateMines.getPlugin().getConfig().getDouble("Tax-Percentage");
                }
            } else if (privateMine.getTaxPercentage() == 0.0D) {
                return;
            }
            double d = e.getSellPrice();
            if (tax == 0.0D)
                tax = defaultTax;
            double takenTax = d / 100.0D * tax;
            double afterTax = d - takenTax;
            e.setSellPrice(afterTax);
            process(privateMine, takenTax, player);
        } catch (NullPointerException e1) {
            e1.printStackTrace();
        }
    }
}