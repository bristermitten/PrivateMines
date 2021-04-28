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
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class UltraPrisonCoreNPCTrait extends Trait implements Listener {
    private final MineStorage storage = PrivateMines.getPlugin().getStorage();

    private final BukkitCommandManager manager = PrivateMines.getPlugin().getManager();

    private final DecimalFormat df = new DecimalFormat("#.##");

    PrivateMine privateMine;

    @Persist("owner")
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
        if (!e.getNPC().equals(this.npc))
            return;
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (!e.getNPC().equals(this.npc))
            return;
        if (e.getClicker() == null) {
            Bukkit.broadcastMessage("Clicker was null?");
            return;
        }
        e.getClicker().performCommand("sellall");
        e.getClicker().sendMessage("Hi from upc trait");
    }

    private void process(PrivateMine privateMine, double tax, Player player) {
        PrivateMines.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(owner), tax);

        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);
        BukkitCommandIssuer ownerIssuer = manager.getCommandIssuer(Bukkit.getPlayer(owner));

        manager.sendMessage(issuer, MessageType.INFO, LangKeys.INFO_TAX_TAKEN,
                "{amount}", String.valueOf(df.format(tax)),
                "{tax}", String.valueOf(privateMine.getTax()));

        if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(owner))) {

            manager.sendMessage(ownerIssuer, MessageType.INFO, LangKeys.INFO_TAX_RECIEVED,
                    "{amount}", String.valueOf(df.format(tax)),
                    "{tax}", String.valueOf(privateMine.getTax()));
        }
    }

    private boolean eventIsNotApplicable(Player player) {
        if (player.getUniqueId().equals(owner))
            return false;
        privateMine = storage.get(owner);
        if (privateMine == null)
            return true;
        return !privateMine.contains(player);
    }

    @EventHandler
    public void onUPCAutoSellEvent(UltraPrisonAutoSellEvent e) {
        Player player = e.getPlayer();
        privateMine = storage.get(owner);
        UltraPrisonCore core = UltraPrisonCore.getInstance();
        UltraPrisonAutoSell autoSell = new UltraPrisonAutoSell(core);
        if (autoSell.isEnabled()) {
            double earnings = autoSell.getCurrentEarnings(player);
            double taxEarnings = earnings / 100.0D * privateMine.getTaxPercentage();
            e.setMoneyToDeposit(earnings);
            process(privateMine, taxEarnings, player);
        } else {
            return;
        }
    }

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
