package me.bristermitten.privatemines.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Pmine implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
        if (sender instanceof Player) {

        	Player p = (Player) sender;
        	//Player target = Bukkit.getPlayer(args[0]);
        	
        	if (cmd.getName().equalsIgnoreCase("pmine")) {
        		
        		if (args.length == 0) {
        			if (p.hasPermission("privatemines.owner")) {
        				
        				Inventory inv = Bukkit.createInventory(null, 18, "Private Mines Owner Menu");
        				
        				ItemStack blocktype = new ItemStack(Material.STONE);
        				ItemMeta typeB = blocktype.getItemMeta();
        				
        				List<String> lore = new ArrayList<String>();

        				lore.add(ChatColor.AQUA + "Click me to choose");
						lore.add(ChatColor.AQUA + "Your mine block type");
						lore.add(ChatColor.AQUA + "Current: " + ChatColor.YELLOW + "GOLD");
        				
        				typeB.setDisplayName(ChatColor.RED + "Block Type");
        				typeB.setLore(lore);
        				
        				blocktype.setItemMeta(typeB);
        				
        				inv.setItem(13, blocktype);
        				
        				p.openInventory(inv);
        			} else {
        				if (!p.hasPermission("privatemines.owner")) {
            				Inventory inv2 = Bukkit.createInventory(null, 9 * 3, "You don't own a Private Mine!");
            				
            				ItemStack no = new ItemStack(Material.REDSTONE_BLOCK);
            				ItemMeta nope = no.getItemMeta();
            				
            				List<String> lore = new ArrayList<String>();

            				lore.add(" ");
            				lore.add(ChatColor.AQUA + "You currently don't own a Private Mine!");
            				lore.add(ChatColor.AQUA + "If you'd like to mine by yourself feel free");
            				lore.add(ChatColor.AQUA + "to purchase one over at store.examplestore.com");
            				
            				nope.setDisplayName(ChatColor.RED + "You don't own a Private Mine!");
            				nope.setLore(lore);
            				
            				no.setItemMeta(nope);
            				
            				inv2.setItem(13, no);
            				
            				p.openInventory(inv2);
        				}
        			}
        		}
        	}
	}
		return false;
	}
}
