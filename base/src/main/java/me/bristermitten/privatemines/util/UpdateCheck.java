package me.bristermitten.privatemines.util;


import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.BiConsumer;

public class UpdateCheck {

    private static final String SPIGOT_URL = "https://api.spigotmc.org/legacy/update.php?resource=%d";

    private final JavaPlugin javaPlugin;

    private String currentVersion;
    private String spigotVersion;
    private int resourceId = -1;
    private BiConsumer<VersionResponse, String> versionResponse;

    private UpdateCheck(@Nonnull JavaPlugin javaPlugin) {
        this.javaPlugin = Objects.requireNonNull(javaPlugin, "javaPlugin");
        this.currentVersion = javaPlugin.getDescription().getVersion();
    }

    public static UpdateCheck of(@Nonnull JavaPlugin javaPlugin) {
        return new UpdateCheck(javaPlugin);
    }

    public UpdateCheck currentVersion(@Nonnull String currentVersion) {
        this.currentVersion = currentVersion;
        return this;
    }

    public UpdateCheck resourceId(int resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public UpdateCheck handleResponse(@Nonnull BiConsumer<VersionResponse, String> versionResponse) {
        this.versionResponse = versionResponse;
        return this;
    }

    public String checkUpdate() {

        URL url;
        String version = null;
        try {
            url = new URL("https://api.spigotmc.org/legacy/update.php?resource=90890");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            line = in.toString();
            version = line;
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return version;
    }

    public String getSpigotVersion() {
        return spigotVersion;
    }
}