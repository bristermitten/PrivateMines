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
import me.bristermitten.privatemines.util.Signs.SignMenuFactory;
import me.bristermitten.privatemines.util.UpdateCheck;
import me.bristermitten.privatemines.view.MenuFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.String.valueOf;

@CommandAlias("privatemines|privatemine|pm|pmine")
public class PrivateMinesCommand extends BaseCommand {

    private static final String PLAYER_KEY = "{player}";
    private final PrivateMines plugin;
    private final MenuFactory factory;
    private final MineStorage storage;
    private final PMConfig config;
    private final UpdateCheck check;
    private final SignMenuFactory signMenuFactory;

    public PrivateMinesCommand(PrivateMines plugin, MenuFactory factory, MineStorage storage,
                               PMConfig config, UpdateCheck check, SignMenuFactory signMenu) {
        this.plugin = plugin;
        this.factory = factory;
        this.storage = storage;
        this.config = config;
        this.check = check;
        this.signMenuFactory = signMenu;
    }

    @Default
    @Description("Manage your Private Mine or go to others")
    public void main(Player p) {
        if (p.hasPermission("privatemines.owner")) {
            factory.openMainMenu(p);
            return;
        }
        getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
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
        if (!plugin.isAutoSellEnabled() && !plugin.isUltraPrisonCoreEnabled()) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_TAX_DISABLED);
            return;
        }
        if (plugin.isUltraPrisonCoreEnabled()) {

            final PrivateMine mine = storage.getOrCreate(p);

            if (config.isTaxSignMenusEnabled()) {
                SignMenuFactory.Menu menu = signMenuFactory.newMenu(
                        Arrays.asList("Please enter a", "tax amount:"))
                        .reopenIfFail(true)
                        .response((player, lines) -> {
                            int taxPercent = Integer.parseInt(lines[2]);

                            if (lines[2] == null) {
                                p.sendMessage("Please enter a tax amount.");
                                return false;
                            }
                            if (taxPercent <= 100 || taxPercentage >= 1) {
                                p.sendMessage("Number either too big or too small.");
                                return false;
                            }
                            mine.setTaxPercentage(taxPercentage);
                            getCurrentCommandIssuer().sendInfo(LangKeys.INFO_TAX_SET, "{tax}", taxPercentage.toString());
                            return true;
                        });
                menu.open(p);
                return;
            }

            mine.setTaxPercentage(taxPercentage);
            getCurrentCommandIssuer().sendInfo(LangKeys.INFO_TAX_SET, "{tax}", taxPercentage.toString());
            return;
        }
        final PrivateMine mine = storage.getOrCreate(p);
        if (taxPercentage == null) {
            getCurrentCommandIssuer().sendInfo(LangKeys.INFO_TAX_INFO, "{tax}", valueOf(mine.getTaxPercentage()));
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
    public void give(CommandSender sender, OnlinePlayer target) {
        if (storage.has(target.getPlayer())) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_MINE);
            return;
        }
        storage.getOrCreate(target.getPlayer()).teleport();
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

    @Subcommand("purge")
    @Description("Purge all the Private Mines on the server")
    @CommandPermission("privatemines.purge")
    public void purge(Player player) {
        Set<PrivateMine> mineSet = storage.getAll();
        if (mineSet.isEmpty()) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_NO_MINES);
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Deleting mines...");

        int total = mineSet.size();

        for (PrivateMine mine : mineSet) {
            mine.delete();
        }
        player.sendMessage(format(ChatColor.GREEN + "Deleted a total of %d mines!", total));
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

        p.spigot().sendMessage(new ComponentBuilder("Click to teleport").color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to Teleport")))
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
            return;
        }
        getCurrentCommandIssuer().sendError(LangKeys.INFO_PLAYER_BANNED, PLAYER_KEY, target.player.getName());
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
        mine.fillWEMultiple(mine.getMineBlocks());
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
        mine.fillWEMultiple(mine.getMineBlocks());

        plugin.getManager().getCommandIssuer(target.getPlayer()).sendInfo(LangKeys.INFO_MINE_RESET);
        getCurrentCommandIssuer().sendInfo(LangKeys.INFO_MINE_RESET_OTHER, PLAYER_KEY, target.getPlayer().getName());
    }

    @Subcommand("trusted")
    @CommandPermission("privatemines.trustedlist")
    @CommandCompletion("@players")
    @Description("Lists who's trusted in your Private Mine!")
    public void trusted(Player sender) {
        PrivateMine mine = storage.get(sender);
        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }

        if (mine.getTrustedPlayers().isEmpty()) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_NO_TRUSTED_PLAYERS);
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "Trusted players: ");
        for (UUID s : mine.getTrustedPlayers()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);
            String lastKnownName = offlinePlayer.getName();
            sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.GRAY + lastKnownName);
        }
    }

    @Subcommand("trust")
    @CommandPermission("privatemines.trust")
    @CommandCompletion("@players")
    @Description("Trust players in your mine!")
    public void trust(Player p, OnlinePlayer target) {
        PrivateMine mine = storage.get(p);

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
        if (target.getPlayer() == p) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_CAN_NOT_REMOVE_FROM_OWN_MINE);
            return;
        }

        mine.getTrustedPlayers().remove(target.getPlayer().getUniqueId());
        plugin.getManager().getCommandIssuer(target.getPlayer()).sendInfo(LangKeys.INFO_PLAYER_REMOVED, PLAYER_KEY, target.getPlayer().getName());
        getCurrentCommandIssuer().sendInfo(LangKeys.ERR_YOU_WERE_REMOVED, PLAYER_KEY, target.getPlayer().getName());
        target.getPlayer().performCommand("spawn");
    }

    @Subcommand("upgrade")
    @CommandPermission("privatemines.upgrade")
    @CommandCompletion("@players")
    @Description("Attempt to upgrade private mine schematics")
    public void upgrade(Player p) {

        PrivateMine mine = storage.get(p);

        if (mine == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE);
            return;
        }

        int tier = mine.getMineTier();
        int newTier = tier + 1;

        String upgradeSchematicS = "tier-" + newTier;

        MineSchematic<?> upgradeSchematic = SchematicStorage.getInstance().get(upgradeSchematicS);

        if (upgradeSchematic == null) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_MINE_UPGRADE_ERROR_INVALID_SCHEMATIC);
            return;
        }

        if (newTier == 0) {
            getCurrentCommandIssuer().sendError(LangKeys.ERR_MINE_UPGRADE_ERROR_INVALID_TIER);
        }

        mine.setMineSchematic(upgradeSchematic);
        mine.setMineTier(newTier);
        mine.fillWEMultiple(mine.getMineBlocks());
        mine.teleport(p);
    }

    @Subcommand("fixreset")
    @CommandPermission("privatemines.fixreset")
    @CommandCompletion("@players")
    @Description("Attempts to fix the mines reset times")
    public void fixreset(Player p) {
        p.sendMessage(ChatColor.RED + "Attempting to cancel the mine reset task!");
        try {
            new MineResetTask(plugin, storage).cancel();
        } catch (Exception e) {
            e.printStackTrace();
            p.sendMessage(ChatColor.RED + "An exception occurred!");
            return;
        }
        p.sendMessage(ChatColor.GREEN + "Cancelled task successfully, attempting to start it back up!");
        new MineResetTask(plugin, storage).start();
    }

    @Subcommand("version")
    @CommandPermission("privatemines.version")
    @Description("Gets the current version of Private Mines")
    public void version(Player p) {
        p.sendMessage(ChatColor.GREEN + "Your Private Mines version: v" + ChatColor.GRAY + plugin.getDescription().getVersion());
    }
}

