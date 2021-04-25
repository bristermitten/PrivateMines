package me.bristermitten.privatemines.data.ultraprisoncore;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.api.events.UltraPrisonSellAllEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class UltraPrisonCoreNPCTrait extends Trait implements Listener {

    private final MineStorage storage = PrivateMines.getPlugin().getStorage();
    private final BukkitCommandManager manager = PrivateMines.getPlugin().getManager();
    PrivateMine privateMine;

    @Persist("owner")
    @DelegatePersistence(UUIDPersister.class)
    private UUID owner;
    private Double tax;


    public UltraPrisonCoreNPCTrait() {
        super("UltraPrisonCoreNPC");
    }

    public UltraPrisonCore getCore() {
        return UltraPrisonCore.getInstance();
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent e) {
        if (!e.getNPC().equals(npc)) {
            return;
        }
    }

    /*
      Used when a player right clicks the npc
     */

    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (!e.getNPC().equals(npc)) {
            return;
        }

        if (e.getClicker() == null) {
            Bukkit.broadcastMessage("Clicker was null?");
            return;
        }

        e.getClicker().performCommand("sellall");
    }

    private void process(PrivateMine privateMine, double tax, Player player) {
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);

        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);
        BukkitCommandIssuer ownerIssuer = manager.getCommandIssuer(Bukkit.getPlayer(owner));

        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(tax),
                "{tax}", String.valueOf(privateMine.getTax()));

        if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(owner))) {
            manager.sendMessage(ownerIssuer, MessageType.INFO, LangKeys.INFO_TAX_RECIEVED,
                    "{amount}", String.valueOf(tax),
                    "{tax}", String.valueOf(privateMine.getTax()));
        }
    }

    private boolean eventIsNotApplicable(List<ItemStack> itemsSold, Player player) {
        if (itemsSold.isEmpty()) {
            return true;
        }
        if (player.getUniqueId().equals(owner)) {
            return true;
        }
        PrivateMine privateMine = storage.get(owner);
        if (privateMine == null) {
            return true;
        }
        return !privateMine.contains(player);
    }

    private boolean eventIsNotApplicable(Player player) {
        if (player.getUniqueId().equals(owner)) {
            return false;
        }
        privateMine = storage.get(owner);
        if (privateMine == null) {
            return true;
        }
        return !privateMine.contains(player);
    }


    /*
        UltraPrisonCore Sell System Listeners..
     */

    //TODO Fix autosell...

//    @EventHandler
//    public void onUPCAutoSellEvent(UltraPrisonAutoSellEvent e) {
//        // Is the person selling the owner? If so, return.
//
//        if (eventIsNotApplicableSingle(new ItemStack(e.getBlock().getType()), e.getPlayer())) {
//            return;
//        }
//        Player player = e.getPlayer();
//        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);
//
//        UltraPrisonAutoSell autoSell = new UltraPrisonAutoSell(getCore());
//
//        double earnings = autoSell.getCurrentEarnings(e.getPlayer());
//        double tax = (earnings / 100.0) * privateMine.getTaxPercentage();
//
//        e.setMoneyToDeposit(e.getMoneyToDeposit() - tax);
//        e.getPlayer().sendMessage("Processing UPC auto sell event");
//        player.sendMessage(" ");
//        process(privateMine, tax, e.getPlayer());
//    }


    @EventHandler
    public void onUPCSellAllEvent(UltraPrisonSellAllEvent e) {

        /*
            Need to add when more things are added to this API.
         */

        Player player = e.getPlayer();
        privateMine = storage.get(owner);

        double defaultTax = 0;

        if (eventIsNotApplicable(player)) {
            return;
        }

        try {
            if (privateMine.getTaxPercentage() > 0) {
                try {
                    tax = privateMine.getTaxPercentage();
                } catch (NullPointerException exc) {
                    defaultTax = PrivateMines.getPlugin().getConfig().getDouble("Tax-Percentage");
                }
            } else if (privateMine.getTaxPercentage() == 0) {
                return;
            }
            double d = e.getSellPrice();

            if (tax == 0) {
                tax = defaultTax;
            }
            double takenTax = d / 100.0 * tax;

            double afterTax = d - takenTax;
            e.setSellPrice(afterTax);
            process(privateMine, takenTax, player);
        } catch (NullPointerException e1) {
            // do nothing, its a false exception.
            return;
        }
    }
}

class UUIDPersister implements Persister<UUID> {

    public UUID create(DataKey dataKey) {
        return UUID.fromString(dataKey.getString("UUID"));
    }

    public void save(UUID uuid, DataKey dataKey) {
        dataKey.setString("UUID", uuid.toString());
    }
}