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
import me.drawethree.ultraprisoncore.api.events.UltraPrisonAutoSellEvent;
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

public class SellNPCTrait extends Trait implements Listener
{

    private final MineStorage storage = PrivateMines.getPlugin().getStorage();
    @Persist("owner")
    @DelegatePersistence(UUIDPersister.class)
    private UUID owner;

    public SellNPCTrait()
    {
        super("SellNPC");
    }

    public void setOwner(UUID owner)
    {
        this.owner = owner;
    }

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent e) {
        if (!e.getNPC().equals(npc))
        {
            return;
        }
    }

    /*
      Used when a player right clicks the npc
     */

    @EventHandler
    public void onRightClick(NPCRightClickEvent e)
    {
        if (!e.getNPC().equals(npc))
        {
            return;
        }
        e.getClicker().performCommand("sellall");
    }

    private void process(PrivateMine privateMine, double tax, Player player)
    {
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

    /*
        AutoSell Sell System Listeners..
    */

    @EventHandler
    public void onAutoSell(AutoSellEvent e)
    {
        //No need to tax the owner
        if (e.getPlayer().getUniqueId().equals(owner)) return;

        PrivateMine privateMine = storage.get(owner);
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
        PrivateMine privateMine = storage.get(owner);

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
        if (player.getUniqueId().equals(owner))
        {
            return true;
        }
        PrivateMine privateMine = storage.get(owner);
        if (privateMine == null)
        {
            return true;
        }
        return !privateMine.contains(player);
    }

    private boolean eventIsNotApplicableSingle(ItemStack itemsSold, Player player)
    {
        if (itemsSold == null)
        {
            return true;
        }
        if (player.getUniqueId().equals(owner))
        {
            player.sendMessage("return thing because of OWNER!");
            return false;
        }
        PrivateMine privateMine = storage.get(owner);
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
        PrivateMine privateMine = storage.get(owner);
        double tax = (e.getTotalCost() / 100.0) * privateMine.getTaxPercentage();
        e.setTotalCost(e.getTotalCost() - tax);
        process(privateMine, tax, e.getPlayer());
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
        double tax = (e.getMoneyToDeposit() / 100.0) * privateMine.getTaxPercentage();
        e.setMoneyToDeposit(e.getMoneyToDeposit() - tax);
        e.getPlayer().sendMessage("Processing UPC sell event");
        process(privateMine, tax, e.getPlayer());
    }

    @EventHandler
    public void onUPCSellAllEvent(UltraPrisonSellAllEvent e) {

        /*
            Need to add when more things are added to this API.
         */

        PrivateMine privateMine = storage.get(owner);

    }

    public static class UUIDPersister implements Persister<UUID>
    {

        public UUID create(DataKey dataKey)
        {
            return UUID.fromString(dataKey.getString("UUID"));
        }

        public void save(UUID uuid, DataKey dataKey)
        {
            dataKey.setString("UUID", uuid.toString());
        }
    }
}