package me.bristermitten.privatemines;

import co.aikar.commands.PaperCommandManager;
import me.bristermitten.privatemines.commands.PrivateMinesCommand;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.service.MineFactory;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.service.MineWorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrivateMines extends JavaPlugin {

    private MineStorage storage;
    private PMConfig config;
    private MineWorldManager manager;
    private MineFactory factory;

    @Override
    public void onEnable() {
        saveDefaultConfig();


        this.storage = new MineStorage();
        this.config = new PMConfig(getConfig());
        this.manager = new MineWorldManager(config);
        this.factory = new MineFactory(this, storage, manager, config);


        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new PrivateMinesCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
