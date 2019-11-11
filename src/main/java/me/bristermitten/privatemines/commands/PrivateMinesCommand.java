package me.bristermitten.privatemines.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import me.bristermitten.privatemines.service.MineStorage;
import org.bukkit.entity.Player;

@CommandAlias("privatemines|privatemine|pm")
public class PrivateMinesCommand extends BaseCommand {

    private final MineStorage storage;

    public PrivateMinesCommand(MineStorage storage) {
        this.storage = storage;
    }

    @Default
    public void mainCommand(Player p) {
        storage
                .getOrCreate(p)
                .teleport();
    }
}
