package me.bristermitten.privatemines;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import me.bristermitten.privatemines.commands.PrivateMinesCommand;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.SellNPCTrait;
import me.bristermitten.privatemines.listeners.BlockBreak;
import me.bristermitten.privatemines.service.MineFactory;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.service.SchematicStorage;
import me.bristermitten.privatemines.view.MenuFactory;
import me.bristermitten.privatemines.world.MineWorldManager;
import me.bristermitten.privatemines.worldedit.WorldEditHook;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class PrivateMines extends JavaPlugin {

    public static final String MINES_FILE_NAME = "mines.yml";
    private Economy econ;
    private MineStorage storage;
    private MenuConfig menuConfig;
    private YamlConfiguration minesConfig;
    private BukkitCommandManager manager;
    private MineFactory<MineSchematic<?>, ?> factory;

    private WorldEditHook weHook;
    private MineWorldManager mineManager;
    private boolean autoSellEnabled = false;
    private boolean npcsEnabled = false;

    public static PrivateMines getPlugin() {
        return JavaPlugin.getPlugin(PrivateMines.class);
    }

    public static Economy getEconomy() {
        return getPlugin().econ;
    }

    public MineWorldManager getMineManager() {
        return mineManager;
    }

    public boolean isAutoSellEnabled() {
        return autoSellEnabled;
    }

    public boolean isNpcsEnabled() {
        return npcsEnabled;
    }

    public WorldEditHook getWeHook() {
        return weHook;
    }

    /*
        Used when the plugin is enabled at the start, loads all the services.
    */

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PMConfig mainConfig = new PMConfig(getConfig());
        mineManager = new MineWorldManager(mainConfig);

        loadWEHook();
        setupEconomy();

        //noinspection unchecked oh no
        factory = (MineFactory<MineSchematic<?>, ?>) loadMineFactory(mainConfig, mineManager);

        this.storage = new MineStorage(factory);

        try {
            loadFiles();
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("An error occurred loading data!");
            e.printStackTrace();
        }

        MenuFactory menuFactory = new MenuFactory(storage, this, menuConfig, mainConfig);

        loadCommands(mainConfig, menuFactory);

        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            npcsEnabled = mainConfig.isNpcsEnabled();
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SellNPCTrait.class).withName("SellNPC"));
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AutoSell")) {
            autoSellEnabled = true;
        }

        if (!setupEconomy()) {
            getLogger().severe(() -> String.format("[%s] - Disabled due to no Vault dependency found!",
                    getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            Bukkit.getLogger().info("Found PlaceholderAPI, you can use those placeholders!");
        } else {
            Bukkit.getLogger().info("Couldn't find PlaceholderAPI. But you can still use the plugin,");
            Bukkit.getLogger().info("you can download the plugin from the spigot website at:");
            Bukkit.getLogger().info("https://www.spigotmc.org/resources/placeholderapi.6245/");
        }

        Bukkit.getPluginManager().registerEvents(new BlockBreak(storage), this);

        new MineResetTask(this, storage).start();
    }

    private void loadWEHook() {
        final String version = Bukkit.getPluginManager().getPlugin("WorldEdit").getDescription().getVersion();
        try {
            if (version.startsWith("6.")) {
                this.weHook = (WorldEditHook) Class.forName("me.bristermitten.privatemines.worldedit.LegacyWEHook")
                        .getConstructor().newInstance();
            } else if (version.startsWith("7.") || version.startsWith("1.1")) { //FAWE 1.13, 1.16, etc
                this.weHook = (WorldEditHook) Class.forName("me.bristermitten.privatemines.worldedit.ModernWEHook")
                        .getConstructor().newInstance();
            } else {
                throw new IllegalStateException("Unsupported WorldEdit version: " + version);
            }
        } catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    @NotNull
    private MineFactory<?, ?> loadMineFactory(PMConfig mainConfig, MineWorldManager mineManager) {
        return new MineFactory(this, mineManager, mainConfig, weHook.createMineFactoryCompat());
    }

    private void loadCommands(PMConfig mainConfig, MenuFactory menuFactory) {
        manager = new PaperCommandManager(this);
        manager.getLocales().addBundleClassLoader(getClassLoader());

        mainConfig.getColors().forEach(manager::setFormat);

        //noinspection deprecation
        manager.enableUnstableAPI("help");

        manager.registerCommand(new PrivateMinesCommand(this, menuFactory, storage, mainConfig));

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
        YamlConfiguration schematicsConfig = new YamlConfiguration();
        saveResource("schematics/schematics.yml", false);
        schematicsConfig.load(new File(getDataFolder(), "schematics/schematics.yml"));

        SchematicStorage.getInstance().loadAll(schematicsConfig);

        getLogger().info("Loaded schematics.yml");

        saveResource(MINES_FILE_NAME, false);

        minesConfig = new YamlConfiguration();
        minesConfig.load(new File(getDataFolder(), MINES_FILE_NAME));
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
        minesConfig.save(new File(getDataFolder(), MINES_FILE_NAME));
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

    public BukkitCommandManager getManager() {
        return manager;
    }

    public MineFactory<MineSchematic<?>, ?> getFactory() {
        return factory;
    }
}
