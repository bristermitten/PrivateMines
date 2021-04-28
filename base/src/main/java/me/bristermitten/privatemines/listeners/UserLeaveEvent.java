package me.bristermitten.privatemines.listeners;

import me.bristermitten.privatemines.util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserLeaveEvent implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Util.removeFromOnlinePlayers(event.getPlayer());
    }
}
