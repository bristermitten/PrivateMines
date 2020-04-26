//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package me.bristermitten.privatemines.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import me.bristermitten.privatemines.Util.color
import me.bristermitten.privatemines.Util.prettify
import me.bristermitten.privatemines.config.LangKeys
import me.bristermitten.privatemines.service.MineStorage
import me.bristermitten.privatemines.view.MenuFactory
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import org.apache.commons.lang.Validate
import org.bukkit.entity.Player

@CommandAlias("privatemines|privatemine|pm|pmine")
class PrivateMinesCommand(
        private val factory: MenuFactory,
        private val storage: MineStorage
) : BaseCommand() {

    @Default
    @Description("Manage your Private Mine or go to others")
    fun main(p: Player) {
        if (p.hasPermission("privatemines.owner")) {
            factory.openMainMenu(p)
        } else {
            factory.createAndOpenMinesMenu(p)
        }
    }

    @HelpCommand
    fun help(issuer: CommandIssuer) {
        @Suppress("DEPRECATION")
        commandHelp.showHelp(issuer)
    }

    @Subcommand("tax")
    @CommandPermission("privatemines.owner")
    @Description("Set your mine's tax percentage")
    fun tax(p: Player, @Optional @Conditions("limits:min=0,max=100") taxPercentage: Double?) {
        val mine = storage.getOrCreate(p)
        if (taxPercentage == null) {
            currentCommandIssuer.sendInfo(LangKeys.INFO_TAX_INFO, "{tax}", mine.taxPercentage.toString())
            return
        }
        mine.taxPercentage = taxPercentage
        currentCommandIssuer.sendInfo(LangKeys.INFO_TAX_SET, "{tax}", taxPercentage.toString())
    }

    @Subcommand("open")
    @CommandPermission("privatemines.owner")
    @Description("Allow other players into your mine")
    fun open(p: Player) {
        val mine = storage.getOrCreate(p)
        mine.isOpen = true
        currentCommandIssuer.sendInfo(LangKeys.INFO_MINE_OPENED)
    }

    @Subcommand("close")
    @CommandPermission("privatemines.owner")
    @Description("Close your mine from other players")
    fun close(p: Player) {
        val mine = storage.getOrCreate(p)
        mine.isOpen = false
        currentCommandIssuer.sendInfo(LangKeys.INFO_MINE_CLOSED)
    }

    @Subcommand("give")
    @CommandPermission("privatemines.give")
    @CommandCompletion("@players")
    @Description("Give a Player a PrivateMine")
    fun give(target: OnlinePlayer) {
        val t = target.getPlayer()
        if (storage.has(t)) {
            currentCommandIssuer.sendError(LangKeys.ERR_PLAYER_HAS_MINE)
            return
        }
        storage.getOrCreate(t).teleport()
        currentCommandIssuer.sendInfo(LangKeys.INFO_MINE_GIVEN)
    }

    @Subcommand("delete")
    @CommandPermission("privatemines.delete")
    @CommandCompletion("@players")
    @Description("Delete a Player's PrivateMine")
    fun delete(target: OnlinePlayer) {
        val targetPlayer = target.getPlayer()
        val mine = storage[targetPlayer]

        if (mine == null) {
            currentCommandIssuer.sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE)
            return
        }
        mine.delete()
        storage.remove(targetPlayer)
    }

    @Subcommand("delete")
    @Description("Delete your PrivateMine")
    fun delete(sender: Player?) {
        val mine = storage[sender]
        if (mine == null) {
            currentCommandIssuer.sendError(LangKeys.ERR_SENDER_HAS_NO_MINE)
            return
        }
        mine.delete()
        storage.remove(sender)
    }

    @Subcommand("status")
    @CommandPermission("privatemines.status")
    @CommandCompletion("@players")
    @Description("View info about a Player's Private Mine")
    fun status(p: Player, target: OnlinePlayer) {
        val t = target.getPlayer()
        val mine = storage[t]
        if (mine == null) {
            currentCommandIssuer.sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE)
            return
        }
        p.sendMessage(("&7Block Type: &6${mine.block.name.prettify()}").color())

        p.spigot().sendMessage(*ComponentBuilder("Click to teleport").color(ChatColor.GOLD)
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Click to Teleport").create()))
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pm teleport " + t.name))
                .create())
    }

    @Subcommand("teleport")
    @CommandPermission("privatemines.teleport")
    @CommandCompletion("@players")
    @Description("Teleport to a Player's PrivateMine")
    fun teleport(p: Player, target: OnlinePlayer) {
        val t = target.getPlayer()
        val mine = storage[t]
        if (mine == null) {
            currentCommandIssuer.sendError(LangKeys.ERR_PLAYER_HAS_NO_MINE)
            return
        }
        mine.teleport(p)
    }
}
