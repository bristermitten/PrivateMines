//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.data;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
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

public class SellNPCTrait extends Trait {

    @Persist("owner")
    @DelegatePersistence(SellNPCTrait.UUIDPersister.class)
    private UUID owner;

    private MineStorage storage = PrivateMines.getPlugin(PrivateMines.class).getStorage();

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
        e.getClicker().performCommand("sellall");
    }


    @EventHandler
    public void onAutoSell(AutoSellEvent e) {

        if (e.getPlayer().getUniqueId().equals(owner)) return;
        PrivateMine privateMine = storage.get(owner);
        if (privateMine == null) {
            return;
        }
        if (!privateMine.contains(e.getPlayer())) {
            return;
        }
        e.setMultiplier(1.0D - privateMine.getTaxPercentage() / 100.0D);
        double tax = e.getPrice() / 100.0D * privateMine.getTaxPercentage();
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);

        BukkitCommandManager manager = PrivateMines.getPlugin(PrivateMines.class).getManager();
        BukkitCommandIssuer issuer = manager.getCommandIssuer(e.getPlayer());
        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(tax),
                "{tax}", String.valueOf(privateMine.getTaxPercentage()));
    }

    @EventHandler
    public void onSellAll(SellAllEvent e) {
        if (e.getItemsSold().isEmpty()) return;
        if (e.getPlayer().getUniqueId().equals(owner)) return;
        PrivateMine privateMine = storage.get(owner);
        System.out.println(privateMine);
        if (privateMine == null) {
            return;
        }

        if (!privateMine.contains(e.getPlayer())) {
            return;
        }

        double tax = (e.getTotalCost() / 100.0) * privateMine.getTaxPercentage();
        System.out.println(tax);
        e.setTotalCost(e.getTotalCost() - tax);
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);


        BukkitCommandManager manager = PrivateMines.getPlugin(PrivateMines.class).getManager();
        BukkitCommandIssuer issuer = manager.getCommandIssuer(e.getPlayer());
        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(tax),
                "{tax}", String.valueOf(privateMine.getTaxPercentage()));
        System.out.println("message sent");

    }

    @EventHandler
    public void onSignSell(SignSellEvent e) {
        if (e.getItemsSold().isEmpty()) return;
        if (e.getPlayer().getUniqueId().equals(owner)) return;
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

        BukkitCommandManager manager = PrivateMines.getPlugin(PrivateMines.class).getManager();
        BukkitCommandIssuer issuer = manager.getCommandIssuer(e.getPlayer());
        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(tax),
                "{tax}", String.valueOf(privateMine.getTaxPercentage()));
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
