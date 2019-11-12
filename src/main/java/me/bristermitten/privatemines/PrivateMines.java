package me.bristermitten.privatemines;

import co.aikar.commands.PaperCommandManager;
import me.bristermitten.privatemines.commands.PrivateMinesCommand;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.service.MineFactory;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.world.MineWorldManager;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public final class PrivateMines extends JavaPlugin {

    private MineStorage storage;
    private PMConfig config;
    private MineWorldManager manager;
    private MineFactory factory;

    private YamlConfiguration minesConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();


        this.config = new PMConfig(getConfig());
        this.manager = new MineWorldManager(config);
        this.factory = new MineFactory(this, manager, config);
        this.storage = new MineStorage(factory);

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new PrivateMinesCommand(storage));

        try {
            loadFiles();
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("An error occurred loading data!");
            e.printStackTrace();
        }
    }

    private void loadFiles() throws IOException, InvalidConfigurationException {
        saveResource("mines.yml", false);
        minesConfig = new YamlConfiguration();
        minesConfig.load(new InputStreamReader(getResource("mines.yml")));
        storage.load(minesConfig);
        getLogger().info("Loaded mines.yml");
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
