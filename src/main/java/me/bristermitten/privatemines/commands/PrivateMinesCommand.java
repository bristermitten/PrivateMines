//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package me.bristermitten.privatemines.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.bristermitten.privatemines.Util;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.view.MenuFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;

@CommandAlias("privatemines|privatemine|pm|pmine")
public class PrivateMinesCommand extends BaseCommand {
    private final MenuFactory factory;
    private final MineStorage storage;

    public PrivateMinesCommand(MenuFactory factory, MineStorage storage) {
        this.factory = factory;
        this.storage = storage;
    }

    @Default
    @Description("Manage your Private Mine or go to others")
    public void main(Player p) {
        if (p.hasPermission("privatemines.owner")) {
            factory.createMenu(p);
        } else {
            factory.createMinesMenu(p);
        }
    }


    @SuppressWarnings("deprecation")
    @HelpCommand
    public void help(CommandIssuer issuer) {
        getCommandHelp().showHelp(issuer);
    }

    @Subcommand("tax")
    @CommandPermission("privatemines.owner")
    @Description("Set your mine's tax percentage")
    public void tax(Player p, @Optional @Conditions("limits:min=0,max=100") Double taxPercentage) {
        PrivateMine mine = storage.getOrCreate(p);
        if (taxPercentage == null) {
            getCurrentCommandIssuer().sendInfo(LangKeys.TAX_INFO, "{tax}", String.valueOf(mine.getTaxPercentage()));
            return;
        }
        mine.setTaxPercentage(taxPercentage);
        getCurrentCommandIssuer().sendInfo(LangKeys.TAX_SET, "{tax}", String.valueOf(mine.getTaxPercentage()));
    }

    @Subcommand("open")
    @CommandPermission("privatemines.owner")
    @Description("Allow other players into your mine")
    public void open(Player p) {
        PrivateMine mine = storage.getOrCreate(p);
        mine.setOpen(true);
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_MINE_OPENED);
    }

    @Subcommand("close")
    @CommandPermission("privatemines.owner")
    @Description("Close your mine from other players")
    public void close(Player p) {
        PrivateMine mine = storage.getOrCreate(p);
        mine.setOpen(false);
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_MINE_CLOSED);
    }

    @Subcommand("give")
    @CommandPermission("privatemines.give")
    @CommandCompletion("@players")
    @Description("Give a Player a PrivateMine")
    public void give(OnlinePlayer target) {
        Player t = target.getPlayer();
        if (storage.has(t)) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_MINE);
            return;
        }
        storage.getOrCreate(t).teleport();
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_MINE_GIVEN);
    }

    @Subcommand("delete")
    @CommandPermission("privatemines.delete")
    @CommandCompletion("@players")
    @Description("Delete a Player's PrivateMine")
    public void delete(OnlinePlayer target) {
        Player t = target.getPlayer();
        PrivateMine mine = storage.get(t);

        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }

        mine.delete();
        storage.remove(t);
    }

    @Subcommand("delete")
    @Description("Delete your PrivateMine")
    public void delete(Player sender) {
        PrivateMine mine = storage.get(sender);

        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_SENDER_HAS_NO_MINE);
            return;
        }

        mine.delete();
        storage.remove(sender);
    }

    @Subcommand("status")
    @CommandPermission("privatemines.status")
    @CommandCompletion("@players")
    @Description("View info about a Player's Private Mine")
    public void status(Player p, OnlinePlayer target) {
        Player t = target.getPlayer();
        PrivateMine mine = storage.get(t);

        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }

        p.sendMessage(Util.color("&7Block Type: &6" + Util.prettify(mine.getBlock().name())));
        p.spigot().sendMessage(new ComponentBuilder("").color(ChatColor.GOLD).
                append("Click to teleport").
                event(new ClickEvent(Action.RUN_COMMAND, "/pm teleport " + t.getName()))
                .create());
    }


    @Subcommand("teleport")
    @CommandPermission("privatemines.teleport")
    @CommandCompletion("@players")
    @Description("Teleport to a Player's PrivateMine")
    public void teleport(Player p, OnlinePlayer target) {
        Player t = target.getPlayer();
        PrivateMine mine = storage.get(t);

        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }
        mine.teleport(p);
    }

}
