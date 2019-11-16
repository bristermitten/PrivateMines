package me.bristermitten.privatemines.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import me.bristermitten.privatemines.data.PrivateMine;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.view.MenuFactory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;

import static me.bristermitten.privatemines.Util.color;
import static me.bristermitten.privatemines.Util.prettify;

@CommandAlias("privatemines|privatemine|pm|pmine")
public class PrivateMinesCommand extends BaseCommand {

    private final MenuFactory factory;
    private final MineStorage storage;

    public PrivateMinesCommand(MenuFactory factory, MineStorage storage) {
        this.factory = factory;
        this.storage = storage;
    }

    @Default
    @CommandPermission("privatemines.main")
    public void mainCommand(Player p) {
        factory.createMenu(p);
    }

    @Subcommand("give")
    @CommandPermission("privatemines.give")
    public void give(Player p, OnlinePlayer t) {
        Player target = t.getPlayer();
        if (storage.has(target)) {
            p.sendMessage(color("&cThis player already has a Private Mine."));
            return;
        }

        storage.getOrCreate(target);
        target.sendMessage(color("&aYou've been given a Private Mine! Type /pm to go to it!"));
        p.sendMessage(color("&aPrivate Mine given."));
    }

    @Subcommand("delete")
    @CommandPermission("privatemines.delete")
    public void delete(Player p, OnlinePlayer t) {
        Player target = t.getPlayer();
        if (!storage.has(target)) {
            p.sendMessage(color("&cThis player doesn't have a Private Mine."));
            return;
        }

        PrivateMine mine = storage.get(target);
        mine.delete();
        storage.remove(target);
    }

    @Subcommand("status")
    @CommandPermission("privatemines.status")
    public void status(Player p, OnlinePlayer t) {
        Player target = t.getPlayer();
        if (!storage.has(target)) {
            p.sendMessage(color("&cThis player doesn't have a Private Mine."));
            return;
        }
        PrivateMine mine = storage.get(target);
        p.sendMessage(color("&7Block Type:        &6" + prettify(mine.getBlock().name())));
        p.spigot().sendMessage(new ComponentBuilder("").color(ChatColor.GOLD).
                append("Click to teleport").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "pm teleport " + target.getName())).create());
    }

    @Subcommand("teleport")
    @CommandPermission("privatemines.teleport")
    public void teleport(Player p, OnlinePlayer t) {
        Player target = t.getPlayer();
        if (!storage.has(target)) {
            p.sendMessage(color("&cThis player doesn't have a Private Mine."));
            return;
        }
        PrivateMine mine = storage.get(target);
        mine.teleport(p);
    }
}
