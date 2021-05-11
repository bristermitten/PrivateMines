package me.bristermitten.privatemines.data.citizens.ultraprisoncore;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.MessageType;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.data.citizens.Taxation;
import me.bristermitten.privatemines.service.MineStorage;
import me.drawethree.ultraprisoncore.api.events.UltraPrisonSellAllEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class UltraPrisonListener implements Listener {
    private static final BukkitCommandManager manager = PrivateMines.getPlugin().getManager();
    private final MineStorage storage = PrivateMines.getPlugin().getStorage();
    PrivateMine privateMine;

    UUID owner;

    private Double tax;

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent e) {
        if (!e.getNPC().hasTrait(UltraPrisonCoreNPCTrait.class))
            return;
        e.getClicker().performCommand("sellall");
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent e) {
        if (!e.getNPC().hasTrait(UltraPrisonCoreNPCTrait.class))
            return;
        e.getClicker().performCommand("sellall");
    }


    @EventHandler
    public void onUPCSellAllEvent(UltraPrisonSellAllEvent e) {
        Player player = e.getPlayer();
        privateMine = storage.get(player.getUniqueId());
        owner = privateMine.getOwner();
        BukkitCommandIssuer issuer = manager.getCommandIssuer(player);

        // Yeah, i'm not touching this anymore. yikes, this is a mess

        double defaultTax = PrivateMines.getPlugin().getConfig().getDouble("Tax-Percentage");
        if (privateMine.getTaxPercentage() > 0.0D) {
            tax = privateMine.getTaxPercentage();
            if (tax == 0.0D) {
                tax = defaultTax;
            }
        } else if (privateMine.getTaxPercentage() == 0.0D) {
            return;
        }
        double d = e.getSellPrice();
        if (tax == 0.0D) {
            tax = defaultTax;
        }
        double takenTax = d / 100.0D * tax;
        double afterTax = d - takenTax;

        if (afterTax == 0) {
            manager.sendMessage(issuer, MessageType.ERROR, LangKeys.INFO_YOU_MADE_NO_MONEY);
            return;
        }

        e.setSellPrice(afterTax);
        Taxation.processTax(privateMine, takenTax, player);
    }
}
