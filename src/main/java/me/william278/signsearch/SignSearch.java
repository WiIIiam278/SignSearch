package me.william278.signsearch;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class SignSearch extends JavaPlugin {

    private static final int METRICS_PLUGIN_ID = 12506;

    private static SignSearch instance;

    public static SignSearch getInstance() {
        return instance;
    }

    public void setInstance(SignSearch plugin) {
        instance = plugin;
    }


    @Override
    public void onEnable() {
        // Plugin startup logic
        setInstance(this);

        getLogger().info("Successfully enabled SignSearch version " + getDescription().getVersion());

        getCommand("signsearch").setExecutor(new SearchCommand());

        // Metrics initialisation
        try {
            new Metrics(this, METRICS_PLUGIN_ID);
        } catch (Exception e) {
            getLogger().warning("An exception occurred initialising metrics; skipping.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Disabled SignSearch version " + getDescription().getVersion());
    }
}
