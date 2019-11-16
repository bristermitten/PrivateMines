package me.bristermitten.privatemines;

import co.aikar.commands.PaperCommandManager;
import me.bristermitten.privatemines.commands.PrivateMinesCommand;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.service.MineFactory;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.view.MenuFactory;
import me.bristermitten.privatemines.world.MineWorldManager;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class PrivateMines extends JavaPlugin {

    private MineStorage storage;
    private PMConfig mainConfig;
    private MenuConfig menuConfig;
    private MineWorldManager manager;
    private MineFactory factory;

    private YamlConfiguration minesConfig;

    private MenuFactory menuFactory;

    @Override
    public void onEnable() {
        saveDefaultConfig();


        this.mainConfig = new PMConfig(getConfig());
        this.manager = new MineWorldManager(mainConfig);
        this.factory = new MineFactory(this, manager, mainConfig);
        this.storage = new MineStorage(factory);

        try {
            loadFiles();
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("An error occurred loading data!");
            e.printStackTrace();
        }
        this.menuFactory = new MenuFactory(storage, this, menuConfig, mainConfig);

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new PrivateMinesCommand(menuFactory, storage));
    }

    private void loadFiles() throws IOException, InvalidConfigurationException {
        saveResource("mines.yml", false);
        minesConfig = new YamlConfiguration();
        minesConfig.load(new File(getDataFolder(), "mines.yml"));
        storage.load(minesConfig);
        getLogger().info("Loaded mines.yml");

        saveResource("menus.yml", false);
        YamlConfiguration menusConfig = new YamlConfiguration();
        menusConfig.load(new File(getDataFolder(), "menus.yml"));
        this.menuConfig = new MenuConfig(menusConfig);
        getLogger().info("Loaded menus.yml");
    }

    private void saveFiles() throws IOException {
        storage.save(minesConfig);
        minesConfig.save(new File(getDataFolder(), "mines.yml"));
        getLogger().info("Saved mines.yml");
    }

    @Override
    public void onDisable() {
        try {
            saveFiles();
        } catch (IOException e) {
            getLogger().severe("An error occurred saving data!");
            e.printStackTrace();
        }
    }
}
