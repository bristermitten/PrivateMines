package me.bristermitten.privatemines.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.bristermitten.privatemines.MineResetTask;
import me.bristermitten.privatemines.PrivateMines;
import me.bristermitten.privatemines.config.LangKeys;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.service.SchematicStorage;
import me.bristermitten.privatemines.util.Util;
import me.bristermitten.privatemines.view.MenuFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.UUID;

import static me.bristermitten.privatemines.util.Util.prettify;

@CommandAlias("privatemines|privatemine|pm|pmine")
public class PrivateMinesCommand extends BaseCommand {

    private static final String PLAYER_KEY = "{player}";
    private final PrivateMines plugin;
    private final MenuFactory factory;
    private final MineStorage storage;
    private final PMConfig config;

    public PrivateMinesCommand(PrivateMines plugin, MenuFactory factory, MineStorage storage, PMConfig config) {
        this.plugin = plugin;
        this.factory = factory;
        this.storage = storage;
        this.config = config;
    }

    @Default
    @Description("Manage your Private Mine or go to others")
    public void main(Player p) {
        if (p.hasPermission("privatemines.owner")) {
            factory.openMainMenu(p);
        } else {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
        }
    }

    @Subcommand("list")
    @Description("List all Private Mines")
    @CommandPermission("privatemines.list")
    public void list(Player p) {
        factory.createAndOpenMinesMenu(p);
    }

    @HelpCommand
    public void help(CommandIssuer issuer) {
        //noinspection deprecation
        getCommandHelp().showHelp(issuer);
    }

    @Subcommand("tax")
    @CommandPermission("privatemines.owner")
    @Description("Set your mine's tax percentage")
    public void tax(Player p, @Optional @Conditions("limits:min=0,max=100") Double taxPercentage) {
        if (!plugin.isAutoSellEnabled()) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_TAX_DISABLED);
            return;
        }
        final PrivateMine mine = storage.getOrCreate(p);
        if (taxPercentage == null) {
            getCurrentCommandIssuer().sendInfo(LangKeys.INFO_TAX_INFO, "{tax}", String.valueOf(mine.getTaxPercentage()));
            return;
        }
        mine.setTaxPercentage(taxPercentage);
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_TAX_SET, "{tax}", taxPercentage.toString());
    }


    @Subcommand("open")
    @CommandPermission("privatemines.owner")
    @Description("Allow other players into your mine")
    public void open(Player p) {
        final PrivateMine mine = storage.getOrCreate(p);
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
        Player targetPlayer = target.getPlayer();
        PrivateMine mine = storage.get(targetPlayer);

        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }
        mine.delete();
        storage.remove(targetPlayer);
    }

    @Subcommand("delete")
    @Description("Delete your PrivateMine")
    @CommandPermission("privatemines.owner")
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
        p.sendMessage(Util.color("&7Block Type: &6" + prettify(mine.getBlock().toString())));

        p.spigot().sendMessage(new ComponentBuilder("Click to teleport").color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to Teleport").create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pm teleport " + t.getName()))
                .create());
    }

    @Subcommand("teleport|visit")
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

    @Subcommand("ban|banish|blacklist")
    @CommandPermission("privatemines.ban")
    @CommandCompletion("@players")
    @Description("Ban a player from visiting your PrivateMine")
    public void ban(Player p, OnlinePlayer target) {
        PrivateMine mine = storage.get(p);
        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_SENDER_HAS_NO_MINE);
            return;
        }
        if (mine.ban(target.getPlayer())) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_ALREADY_BANNED, PLAYER_KEY, target.player.getName());
        } else {
            getCurrentCommandIssuer().sendError(LangKeys.INFO_PLAYER_BANNED, PLAYER_KEY, target.player.getName());
        }
    }

    @Subcommand("unban|pardon")
    @CommandPermission("privatemines.ban")
    @CommandCompletion("@players")
    @Description("Unban a player from visiting your PrivateMine")
    public void unban(Player p, OnlinePlayer target) {
        PrivateMine mine = storage.get(p);
        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_SENDER_HAS_NO_MINE);
            return;
        }
        if (mine.unban(target.getPlayer())) {
            getCurrentCommandIssuer().sendError(LangKeys.INFO_PLAYER_UNBANNED, PLAYER_KEY, target.player.getName());
        } else {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_NOT_BANNED, PLAYER_KEY, target.player.getName());
        }
    }

    @Subcommand("reset")
    @CommandPermission("privatemines.reset")
    @Description("Force a mine reset")
    public void reset(Player p) {
        PrivateMine mine = storage.get(p);
        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }

        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_MINE_RESET);
        mine.fill(mine.getBlock());
    }

    @Subcommand("reset")
    @CommandPermission("privatemines.reset.other")
    @CommandCompletion("@players")
    @Description("Force a mine reset for another player")
    public void reset(Player p, OnlinePlayer target) {
        PrivateMine mine = storage.get(target.getPlayer());
        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }
        mine.fill(mine.getBlock());
        plugin.getManager().getCommandIssuer(target.getPlayer()).sendInfo(LangKeys.INFO_MINE_RESET);
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_MINE_RESET_OTHER, PLAYER_KEY, target.getPlayer().getName());
    }

    @Subcommand("trusted")
    @CommandPermission("privatemines.trustedlist")
    @CommandCompletion("@players")
    @Description("Lists who's trusted in your Private Mine!")
    public void trusted(Player p) {
        PrivateMine mine = storage.get(p.getPlayer());
        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }

        if (mine.getTrustedPlayers().isEmpty()) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_NO_TRUSTED_PLAYERS);
            return;
        }

        p.sendMessage(ChatColor.GREEN + "Trusted players: ");
        for (UUID s : mine.getTrustedPlayers()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
            String lastKnownName = offlinePlayer.getName();
            p.sendMessage(ChatColor.GOLD + "- " + ChatColor.GRAY + lastKnownName);
        }
    }

    @Subcommand("trust")
    @CommandPermission("privatemines.trust")
    @CommandCompletion("@players")
    @Description("Trust players in your mine!")
    public void trust(Player p, OnlinePlayer target) {
        PrivateMine mine = storage.get(p.getPlayer());
        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
        World w = Bukkit.getWorld(config.getWorldName());

        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }

        for (UUID s : mine.getTrustedPlayers()) {
            if (s.equals(target.getPlayer().getUniqueId())) {
                getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_ALREADY_ADDED, PLAYER_KEY, target.getPlayer().getName());
                return;
            }
        }

        mine.getTrustedPlayers().add(target.getPlayer().getUniqueId());
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_PLAYER_ADDED, PLAYER_KEY, target.getPlayer().getName());

        for (UUID s : mine.getTrustedPlayers()) {
            p.sendMessage(s.toString());
        }
    }

    @Subcommand("untrust")
    @CommandPermission("privatemines.untrust")
    @CommandCompletion("@players")
    @Description("Untrust a player from your Private Mine")
    public void untrust(Player p, OnlinePlayer target) {
        PrivateMine mine = storage.get(p.getPlayer());
        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }
        if (!mine.getTrustedPlayers().contains(target.getPlayer().getUniqueId())) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_NOT_ON_TRUSTED_LIST);
            return;
        }
        mine.getTrustedPlayers().remove(target.player.getUniqueId());
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_PLAYER_REMOVED, PLAYER_KEY, target.getPlayer().getName());
    }


    @Subcommand("remove")
    @CommandPermission("privatemines.remove")
    @CommandCompletion("@players")
    @Description("Remove players to your Private Mine!")
    public void remove(Player p, OnlinePlayer target) {
        PrivateMine mine = storage.get(p);
        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }
        mine.getTrustedPlayers().remove(target.getPlayer().getUniqueId());
        plugin.getManager().getCommandIssuer(target.getPlayer()).sendInfo(LangKeys.INFO_PLAYER_REMOVED, PLAYER_KEY, target.getPlayer().getName());
        getCurrentCommandIssuer().sendInfo(LangKeys.ERR_YOU_WERE_REMOVED, PLAYER_KEY, target.getPlayer().getName());
        target.getPlayer().performCommand("spawn");
        WorldGuardWrapper.getInstance().getRegion(Bukkit.getWorld(""), "a");
    }

    @Subcommand("upgrade")
    @CommandPermission("privatemines.upgrade")
    @CommandCompletion("@players")
    @Description("Attempt to upgrade private mine schematics")
    public void upgrade(Player p) {

        PrivateMine mine = storage.get(p);
        int tier = mine.getMineTier();
        int newTier = tier + 1;
        p.sendMessage("tier-" + newTier);
        String upgradeSchematicS = "tier-" + newTier;

        MineSchematic<?> upgradeSchematic = SchematicStorage.getInstance().get(upgradeSchematicS);

        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }

        if (upgradeSchematic == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_MINE_UPGRADE_ERROR);
            return;
        }

        if (newTier == 0) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_MINE_UPGRADE_ERROR);
        }

        mine.setMineSchematic(upgradeSchematic);
        mine.setMineTier(newTier);
        mine.fill(mine.getBlock());
        mine.teleport(p);
    }

    @Subcommand("fixreset")
    @CommandPermission("privatemines.fixreset")
    @CommandCompletion("@players")
    @Description("Attempts to fix the mines reset times")
    public void fixreset(Player p) {
        p.sendMessage(ChatColor.RED + "Attempting to cancel the mine reset task!");
        try {
            new MineResetTask(PrivateMines.getPlugin(), storage).cancel();
        } catch (Exception e) {
            e.printStackTrace();
            p.sendMessage(ChatColor.RED + "An exception occurred!");
            return;
        }
        p.sendMessage(ChatColor.GREEN + "Cancelled task successfully, attempting to start it back up!");
        new MineResetTask(PrivateMines.getPlugin(), storage).start();
    }
}
