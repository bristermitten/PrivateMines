package me.bristermitten.privatemines.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.Util;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.view.MenuFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

import static me.bristermitten.privatemines.Util.prettify;

@CommandAlias("privatemines|privatemine|pm|pmine")
public class PrivateMinesCommand extends BaseCommand
{

    private final PrivateMines plugin;
    private final MenuFactory factory;
    private final MineStorage storage;

    public PrivateMinesCommand(PrivateMines plugin, MenuFactory factory, MineStorage storage)
    {
        this.plugin = plugin;
        this.factory = factory;
        this.storage = storage;
    }

    @Default
    @Description("Manage your Private Mine or go to others")
    public void main(Player p)
    {
        if (p.hasPermission("privatemines.owner"))
        {
            factory.openMainMenu(p);
        } else
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
        }
    }

    @Subcommand("list")
    @Description("List all Private Mines")
    @CommandPermission("privatemines.list")
    public void list(Player p)
    {
        factory.createAndOpenMinesMenu(p);
    }

    @HelpCommand
    public void help(CommandIssuer issuer)
    {
        //noinspection deprecation
        getCommandHelp().showHelp(issuer);
    }

    @Subcommand("tax")
    @CommandPermission("privatemines.owner")
    @Description("Set your mine's tax percentage")
    public void tax(Player p, @Optional @Conditions("limits:min=0,max=100") Double taxPercentage)
    {
        if (!plugin.isAutoSellEnabled())
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_TAX_DISABLED);
            return;
        }
        final PrivateMine mine = storage.getOrCreate(p);
        if (taxPercentage == null)
        {
            getCurrentCommandIssuer().sendInfo(LangKeys.INFO_TAX_INFO, "{tax}", String.valueOf(mine.getTaxPercentage()));
            return;
        }
        mine.setTaxPercentage(taxPercentage);
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_TAX_SET, "{tax}", taxPercentage.toString());
    }


    @Subcommand("open")
    @CommandPermission("privatemines.owner")
    @Description("Allow other players into your mine")
    public void open(Player p)
    {
        final PrivateMine mine = storage.getOrCreate(p);
        mine.setOpen(true);
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_MINE_OPENED);
    }


    @Subcommand("close")
    @CommandPermission("privatemines.owner")
    @Description("Close your mine from other players")
    public void close(Player p)
    {
        PrivateMine mine = storage.getOrCreate(p);
        mine.setOpen(false);
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_MINE_CLOSED);
    }

    @Subcommand("give")
    @CommandPermission("privatemines.give")
    @CommandCompletion("@players")
    @Description("Give a Player a PrivateMine")
    public void give(OnlinePlayer target)
    {
        Player t = target.getPlayer();
        if (storage.has(t))
        {
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
    public void delete(OnlinePlayer target)
    {
        Player targetPlayer = target.getPlayer();
        PrivateMine mine = storage.get(targetPlayer);

        if (mine == null)
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }
        mine.delete();
        storage.remove(targetPlayer);
    }

    @Subcommand("delete")
    @Description("Delete your PrivateMine")
    @CommandPermission("privatemines.owner")
    public void delete(Player sender)
    {
        PrivateMine mine = storage.get(sender);
        if (mine == null)
        {
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
    public void status(Player p, OnlinePlayer target)
    {
        Player t = target.getPlayer();
        PrivateMine mine = storage.get(t);
        if (mine == null)
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }
        p.sendMessage(Util.color("&7Block Type: &6" + prettify(mine.getBlock().name())));

        p.spigot().sendMessage(new ComponentBuilder("Click to teleport").color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to Teleport").create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pm teleport " + t.getName()))
                .create());
    }

    @Subcommand("teleport|visit")
    @CommandPermission("privatemines.teleport")
    @CommandCompletion("@players")
    @Description("Teleport to a Player's PrivateMine")
    public void teleport(Player p, OnlinePlayer target)
    {
        Player t = target.getPlayer();
        PrivateMine mine = storage.get(t);
        if (mine == null)
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }
        mine.teleport(p);
    }

    @Subcommand("ban|banish|blacklist")
    @CommandPermission("privatemines.ban")
    @CommandCompletion("@players")
    @Description("Ban a player from visiting your PrivateMine")
    public void ban(Player p, OnlinePlayer target)
    {
        PrivateMine mine = storage.get(p);
        if (mine == null)
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_SENDER_HAS_NO_MINE);
            return;
        }
        if (mine.ban(target.getPlayer()))
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_ALREADY_BANNED, "{player}", target.player.getName());
        } else
        {
            getCurrentCommandIssuer().sendError(LangKeys.INFO_PLAYER_BANNED, "{player}", target.player.getName());
        }
    }

    @Subcommand("unban|pardon")
    @CommandPermission("privatemines.ban")
    @CommandCompletion("@players")
    @Description("Unban a player from visiting your PrivateMine")
    public void unban(Player p, OnlinePlayer target)
    {
        PrivateMine mine = storage.get(p);
        if (mine == null)
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_SENDER_HAS_NO_MINE);
            return;
        }
        if (mine.unban(target.getPlayer()))
        {
            getCurrentCommandIssuer().sendError(LangKeys.INFO_PLAYER_UNBANNED, "{player}", target.player.getName());
        } else
        {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_NOT_BANNED, "{player}", target.player.getName());
        }
    }
}
