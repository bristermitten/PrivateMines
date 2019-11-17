//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.data;

import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.service.MineStorage;
import me.clip.autosell.events.AutoSellEvent;
import me.clip.autosell.events.SellAllEvent;
import me.clip.autosell.events.SignSellEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SellNPCTrait extends Trait {
    @Persist("owner")
    @DelegatePersistence(SellNPCTrait.UUIDPersister.class)
    private UUID owner;
    private MineStorage storage = (PrivateMines.getPlugin(PrivateMines.class)).getStorage();
    private AtomicBoolean cooldown = new AtomicBoolean(false);

    public SellNPCTrait() {
        super("SellNPC");
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @EventHandler
    public void onClick(NPCRightClickEvent e) {
        if (!e.getNPC().equals(npc)) {
            return;
        }
        if (cooldown.get()) {
            return;
        }
        e.getClicker().performCommand("sellall");
        cooldown.set(true);
        Bukkit.getScheduler().runTaskLaterAsynchronously(PrivateMines.getPlugin(PrivateMines.class),
                () -> cooldown.set(false), 20L);
    }

    @EventHandler
    public void onAutoSell(AutoSellEvent e) {
        PrivateMine privateMine = this.storage.get(this.owner);
        if (privateMine == null) {
            return;
        }
        if (!privateMine.contains(e.getPlayer())) {
            return;
        }
        e.setMultiplier(1.0D - privateMine.getTaxPercentage() / 100.0D);
        double tax = e.getPrice() / 100.0D * privateMine.getTaxPercentage();
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(this.owner), tax);
    }

    @EventHandler
    public void onSellAll(SellAllEvent e) {
        PrivateMine privateMine = storage.get(owner);
        if (privateMine == null) {
            return;
        }
        if (!privateMine.contains(e.getPlayer())) {
            return;
        }
        double tax = (e.getTotalCost() / 100.0) * privateMine.getTaxPercentage();
        e.setTotalCost(e.getTotalCost() - tax);
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);
    }

    @EventHandler
    public void onSignSell(SignSellEvent e) {
        PrivateMine privateMine = storage.get(owner);
        if (privateMine == null) {
            return;
        }
        if (!privateMine.contains(e.getPlayer())) {
            return;
        }
        double tax = (e.getTotalCost() / 100.0) * privateMine.getTaxPercentage();
        e.setTotalCost(e.getTotalCost() - tax);
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);
    }

    public static class UUIDPersister implements Persister<UUID> {
        public UUIDPersister() {
        }

        public UUID create(DataKey dataKey) {
            return UUID.fromString(dataKey.getString("UUID"));
        }

        public void save(UUID uuid, DataKey dataKey) {
            dataKey.setString("UUID", uuid.toString());
        }
    }
}
