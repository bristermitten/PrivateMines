package me.bristermitten.privatemines.listeners;

import me.bristermitten.privatemines.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UserJoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Util.addToOnlinePlayers(event.getPlayer());
    }
}
