package me.bristermitten.privatemines.data.UltraPrisonCore;

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
import me.drawethree.ultraprisoncore.autosell.AutoSellRegion;
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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UltraPrisonCoreNPCTrait extends Trait implements Listener {

    private final MineStorage storage = PrivateMines.getPlugin().getStorage();
    @Persist("owner")
    @DelegatePersistence(UUIDPersister.class)
    private UUID owner;

    public UltraPrisonCore getCore() {
        return UltraPrisonCore.getInstance();
    }

    public UltraPrisonCoreNPCTrait() {
        super("UltraPrisonCoreNPC");
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

        BukkitCommandManager manager = PrivateMines.getPlugin().getManager();
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
        PrivateMine privateMine = storage.get(owner);
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

        PrivateMine privateMine = storage.get(owner);
        UltraPrisonAutoSell autoSell = new UltraPrisonAutoSell(getCore());

        double earnings = autoSell.getCurrentEarnings(e.getPlayer());
        double tax = (earnings / 100.0) * privateMine.getTaxPercentage();
        
        e.setMoneyToDeposit(e.getMoneyToDeposit() - tax);
        e.getPlayer().sendMessage("Processing UPC auto sell event");
        e.getPlayer().sendMessage("Earnings: " + earnings);
        e.getPlayer().sendMessage("Tax: " + tax);
        process(privateMine, tax, e.getPlayer());
    }


    @EventHandler
    public void onUPCSellAllEvent(UltraPrisonSellAllEvent e) {

        /*
            Need to add when more things are added to this API.
         */

        e.getPlayer().sendMessage("Sell All has successfully fired from UPC.");
        PrivateMine privateMine = storage.get(owner);
        double taxPercent = privateMine.getTaxPercentage();

        AutoSellRegion region = e.getRegion();
        Set<Material> sellingMaterials = e.getRegion().getSellingMaterials();

        double d = 0.0D;

        for (Material material : sellingMaterials) {
            d = sellingMaterials.size() * region.getSellPriceFor(material);
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "UltraPrisonSellAllEvent d before tax = " + d);

        if (taxPercent == 0) {
            Bukkit.broadcastMessage("Don't do the tax thing and just give the money!");
            e.getPlayer().sendMessage("You get $" + d + " because there's no tax");
            return;
        } else {
            double tax = d / 100.0 * privateMine.getTaxPercentage();

            e.getPlayer().sendMessage(ChatColor.RED + "Taken $" + tax + " in tax.");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getUniqueId().equals(owner)) {
                    p.sendMessage(ChatColor.GREEN + "You've received $" + tax + " tax from " + e.getPlayer().getName());
                }

                double afterTax = d - tax;
                p.sendMessage("After tax part: " + afterTax);
            }
        }
    }

    public static class UUIDPersister implements Persister<UUID> {

        public UUID create(DataKey dataKey) {
            return UUID.fromString(dataKey.getString("UUID"));
        }

        public void save(UUID uuid, DataKey dataKey) {
            dataKey.setString("UUID", uuid.toString());
        }
    }
}