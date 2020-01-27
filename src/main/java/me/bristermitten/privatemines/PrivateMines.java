package me.bristermitten.privatemines;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import me.bristermitten.privatemines.commands.PrivateMinesCommand;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.data.SellNPCTrait;
import me.bristermitten.privatemines.service.MineFactory;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.view.MenuFactory;
import me.bristermitten.privatemines.world.MineWorldManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class PrivateMines extends JavaPlugin {

    private MineStorage storage;
    private MenuConfig menuConfig;

    private YamlConfiguration minesConfig;

    private static Economy econ;

    public static Economy getEconomy() {
        return econ;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PMConfig mainConfig = new PMConfig(getConfig());
        MineWorldManager mineManager = new MineWorldManager(mainConfig);
        MineFactory factory = new MineFactory(this, mineManager, mainConfig);
        this.storage = new MineStorage(factory);

        try {
            loadFiles();
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("An error occurred loading data!");
            e.printStackTrace();
        }

        MenuFactory menuFactory = new MenuFactory(storage, this, menuConfig, mainConfig);

        loadCommands(mainConfig, menuFactory);

        if (!CitizensAPI.getTraitFactory().getRegisteredTraits().contains(SellNPCTrait.class)) {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SellNPCTrait.class).withName("SellNPC"));
        } else {
            return;
        }
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!",
                    getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void loadCommands(PMConfig mainConfig, MenuFactory menuFactory) {
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.getLocales().addBundleClassLoader(getClassLoader());

        mainConfig.getColors().forEach(manager::setFormat);

        //noinspection deprecation
        manager.enableUnstableAPI("help");

        manager.registerCommand(new PrivateMinesCommand(menuFactory, storage));
        
        manager.getCommandConditions().addCondition(Double.class, "limits", (c, exec, value) -> {
            if (value == null) {
                return;
            }
            if (c.hasConfig("min") && c.getConfigValue("min", 0) > value) {
                throw new ConditionFailedException("Value must be >" + c.getConfigValue("min", 0));
            }
            if (c.hasConfig("max") && c.getConfigValue("max", 0) < value) {
                throw new ConditionFailedException(
                        "Value must be <" + c.getConfigValue("max", 0));
            }
        });
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        } else {
            RegisteredServiceProvider<Economy> rsp =
                    getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            } else {
                econ = rsp.getProvider();
                return econ != null;
            }
        }
    }
    
    public MineStorage getStorage() {
        return storage;
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
