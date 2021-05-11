package me.bristermitten.privatemines.data.citizens.autosell;

import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.data.citizens.Taxation;
import me.bristermitten.privatemines.service.MineStorage;
import me.clip.autosell.events.AutoSellEvent;
import me.clip.autosell.events.SellAllEvent;
import me.clip.autosell.events.SignSellEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;

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


    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (!e.getNPC().hasTrait(AutoSellNPCTrait.class)) {
            return;
        }
        e.getClicker().performCommand(config.getSellCommand());
    }


    @EventHandler
    public void onAutoSell(AutoSellEvent e) {
        PrivateMine privateMine = storage.get(e.getPlayer());

        if (Taxation.cannotBeTaxed(storage, Collections.singleton(e.getItemStackSold()), e.getPlayer())) {
            return;
        }

        e.setMultiplier(1.0D - privateMine.getTaxPercentage() / 100.0D);
        double tax = e.getPrice() / 100.0D * privateMine.getTaxPercentage();
        Taxation.processTax(privateMine, tax, e.getPlayer());
    }

    @EventHandler
    public void onSellAll(SellAllEvent e) {
        if (Taxation.cannotBeTaxed(storage, e.getItemsSold(), e.getPlayer())) {
            return;
        }
        final PrivateMine privateMine = storage.get(e.getPlayer().getUniqueId());

        double tax = e.getTotalCost() / 100.0 * privateMine.getTaxPercentage();
        e.setTotalCost(e.getTotalCost() - tax);
        Taxation.processTax(privateMine, tax, e.getPlayer());
    }


    @EventHandler
    public void onSignSell(SignSellEvent e) {
        if (Taxation.cannotBeTaxed(storage, e.getItemsSold(), e.getPlayer())) {
            return;
        }
        PrivateMine privateMine = storage.get(e.getPlayer());
        double tax = (e.getTotalCost() / 100.0) * privateMine.getTaxPercentage();
        e.setTotalCost(e.getTotalCost() - tax);
        Taxation.processTax(privateMine, tax, e.getPlayer());
    }
}
