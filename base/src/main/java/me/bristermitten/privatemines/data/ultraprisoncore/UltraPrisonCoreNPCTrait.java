package me.bristermitten.privatemines.data.ultraprisoncore;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.drawethree.ultraprisoncore.UltraPrisonCore;
import me.drawethree.ultraprisoncore.api.events.UltraPrisonAutoSellEvent;
import me.drawethree.ultraprisoncore.api.events.UltraPrisonSellAllEvent;
import me.drawethree.ultraprisoncore.autosell.UltraPrisonAutoSell;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(tax),
                "{tax}", String.valueOf(privateMine.getTaxPercentage()));
        Bukkit.getOfflinePlayer(owner);
        if (Bukkit.getOnlinePlayers().contains(owner)) {
            Bukkit.getPlayer(owner).sendMessage("You've received $" + tax + " from " + player.getName());
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

    private boolean eventIsNotApplicableSingle(ItemStack itemsSold, Player player) {
        if (itemsSold == null) {
            return true;
        }
        if (player.getUniqueId().equals(owner)) {
            player.sendMessage("return thing because of OWNER!");
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

    @EventHandler
    public void onUPCAutoSellEvent(UltraPrisonAutoSellEvent e) {
        // Is the person selling the owner? If so, return.

        if (eventIsNotApplicableSingle(new ItemStack(e.getBlock().getType()), e.getPlayer())) {
            return;
        }
        Player player = e.getPlayer();
        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);

        UltraPrisonAutoSell autoSell = new UltraPrisonAutoSell(getCore());

        double earnings = autoSell.getCurrentEarnings(e.getPlayer());
        double tax = (earnings / 100.0) * privateMine.getTaxPercentage();

        e.setMoneyToDeposit(e.getMoneyToDeposit() - tax);
        e.getPlayer().sendMessage("Processing UPC auto sell event");
        player.sendMessage(" ");

        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(tax),
                "{tax}", String.valueOf(privateMine.getTaxPercentage()));
        process(privateMine, tax, e.getPlayer());
    }


    @EventHandler
    public void onUPCSellAllEvent(UltraPrisonSellAllEvent e) {

        /*
            Need to add when more things are added to this API.
         */


        Player player = e.getPlayer();
        e.getPlayer().sendMessage("Sell All has successfully fired from UPC.");
        privateMine = storage.get(owner);
        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);

        double defaultTax = 0;

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

            Bukkit.broadcastMessage(ChatColor.GREEN + "UltraPrisonSellAllEvent d before tax = " + d);

            if (tax == 0) {
                tax = defaultTax;
            }
            double takenTax = d / 100.0 * tax;

            if (Bukkit.getOnlinePlayers().contains(owner)) {
                BukkitCommandIssuer ownerIssuer = manager.getCommandIssuer(Bukkit.getPlayer(owner));
                manager.sendMessage(ownerIssuer, MessageType.INFO, LangKeys.INFO_TAX_RECIEVED,
                        "{amount}", String.valueOf(takenTax),
                        "{tax}", String.valueOf(privateMine.getMinePercentage()));
            }
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getUniqueId().equals(owner)) {
                    p.sendMessage(ChatColor.GREEN + "You've received $" + takenTax +
                            " tax from " + e.getPlayer().getName());
                }
            }
//
//                double afterTax = d - takenTax;
//                e.setSellPrice(afterTax);
//                p.sendMessage("After tax part: " + afterTax);
//                manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
//                        "{amount}", String.valueOf(tax),
//                        "{tax}", String.valueOf(privateMine.getTaxPercentage()));
//            }
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
