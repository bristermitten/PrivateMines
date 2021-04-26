package me.bristermitten.privatemines;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import me.bristermitten.privatemines.commands.PrivateMinesCommand;
import me.bristermitten.privatemines.config.PMConfig;
import me.bristermitten.privatemines.config.menu.MenuConfig;
import me.bristermitten.privatemines.data.autosell.AutoSellNPCTrait;
import me.bristermitten.privatemines.data.MineSchematic;
import me.bristermitten.privatemines.data.ultraprisoncore.UltraPrisonCoreNPCTrait;
import me.bristermitten.privatemines.listeners.UserJoinEvent;
import me.bristermitten.privatemines.listeners.UserLeaveEvent;
import me.bristermitten.privatemines.service.MineFactory;
import me.bristermitten.privatemines.service.MineStorage;
import me.bristermitten.privatemines.service.SchematicStorage;
import me.bristermitten.privatemines.util.PrivateMinesAPI;
import me.bristermitten.privatemines.util.Signs.SignMenuFactory;
import me.bristermitten.privatemines.util.UpdateCheck;
import me.bristermitten.privatemines.view.MenuFactory;
import me.bristermitten.privatemines.world.MineWorldManager;
import me.bristermitten.privatemines.worldedit.WorldEditHook;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class PrivateMines extends JavaPlugin {

    public static final String MINES_FILE_NAME = "mines.yml";
    private static PrivateMinesAPI privateMinesAPI;

    private static int resourceID = 90890;
    private Economy econ;
    private MineStorage storage;
    private MenuConfig menuConfig;
    private YamlConfiguration minesConfig;
    private BukkitCommandManager manager;
    private MineFactory<MineSchematic<?>, ?> factory;
    private UpdateCheck updateCheck;
    private WorldEditHook weHook;
    private MineWorldManager mineManager;
    private SignMenuFactory signMenuFactory;

    private boolean autoSellEnabled = false;
    private boolean ultraPrisonCoreEnabled = false;
    private boolean protocolLibEnabled = false;

    private boolean npcsEnabled = false;

    public static PrivateMines getPlugin() {
        return JavaPlugin.getPlugin(PrivateMines.class);
    }

    public static Economy getEconomy() {
        return getPlugin().econ;
    }

    public static PrivateMinesAPI getPrivateMinesAPI() {
        return privateMinesAPI;
    }

    public MineWorldManager getMineManager() {
        return mineManager;
    }

    public boolean isAutoSellEnabled() {
        return autoSellEnabled;
    }

    public boolean isUltraPrisonCoreEnabled() {
        return ultraPrisonCoreEnabled;
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
        long startTime = System.currentTimeMillis();

        saveDefaultConfig();

        PMConfig mainConfig = new PMConfig(getConfig());
        mineManager = new MineWorldManager(mainConfig);

        loadWEHook();
        setupEconomy();

        //noinspection unchecked oh no
        factory = (MineFactory<MineSchematic<?>, ?>) loadMineFactory(mainConfig, mineManager);

        this.storage = new MineStorage(factory);
        this.signMenuFactory = new SignMenuFactory(this);

        try {
            loadFiles();
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().severe("An error occurred loading data!");
            e.printStackTrace();
        }

        MenuFactory menuFactory = new MenuFactory(storage, this, menuConfig, mainConfig);

        loadCommands(mainConfig, menuFactory);

        if (Bukkit.getPluginManager().isPluginEnabled("AutoSell") && getConfig().getBoolean("autosell-enabled")) {
            autoSellEnabled = true;
            Bukkit.getLogger().info("Enabled hook for AutoSell!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("UltraPrisonCore") && getConfig().getBoolean("ultraprisoncore-enabled")) {
            ultraPrisonCoreEnabled = true;
            Bukkit.getLogger().info("Enabled hook for UltraPrisonCore!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("AutoSell") && getConfig().getBoolean("autosell-enabled")
                && Bukkit.getPluginManager().isPluginEnabled("UltraPrisonCore") && getConfig().getBoolean("ultraprisoncore-enabled")) {
            Bukkit.getLogger().warning("Both AutoSell and UltraPrisonCore hooks were enabled, please only enable one!");
            Bukkit.getLogger().warning("Disabling PrivateMines!");
            setEnabled(false);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            npcsEnabled = mainConfig.isNpcsEnabled();

            if (autoSellEnabled) {
                Bukkit.getLogger().info("Enabling AutoSell NPC Trait!");
                CitizensAPI.getTraitFactory().registerTrait
                        (TraitInfo.create(AutoSellNPCTrait.class).withName("SellNPC"));
            } else if (ultraPrisonCoreEnabled) {
                Bukkit.getLogger().info("Enabling UltraPrisonCore NPC Trait!");
                CitizensAPI.getTraitFactory().registerTrait
                        (TraitInfo.create(UltraPrisonCoreNPCTrait.class).withName("UltraPrisonCoreNPC"));
            }
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

        Bukkit.getPluginManager().registerEvents(new UserJoinEvent(), this);
        Bukkit.getPluginManager().registerEvents(new UserLeaveEvent(), this);
        Bukkit.getPluginManager().registerEvents(new AutoSellNPCTrait(), this);
        Bukkit.getPluginManager().registerEvents(new UltraPrisonCoreNPCTrait(), this);


        new MineResetTask(this, storage).start();

        long loaded = System.currentTimeMillis();
        Bukkit.getLogger().info(String.format(ChatColor.GREEN +
                        "Successfully loaded PrivateMines (Took %dms)",
                loaded - startTime));
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

        manager.registerCommand(new PrivateMinesCommand(this, menuFactory, storage,
                mainConfig, updateCheck, signMenuFactory));

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
