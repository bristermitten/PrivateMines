package me.bristermitten.privatemines.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrivateMinesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] strings) {

        Player p = (Player) sender;

        String permission = "privatemines.owner";

        if (cmd.getName().equalsIgnoreCase("privatemines")) {
            if (!p.hasPermission(permission)) {
                p.sendMessage("Opening public mines gui.");
            } else {
                p.sendMessage("Opening private mines owner gui.");
            }
        }
        return false;
    }
}
