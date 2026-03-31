package dev.andres.keepeffects;

import org.bukkit.plugin.java.JavaPlugin;

public class KeepEffectsPlugin extends JavaPlugin {

    private static KeepEffectsPlugin instance;
    private EffectsConfig effectsConfig;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        effectsConfig = new EffectsConfig(this);

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);

        KeepEffectsCommand cmd = new KeepEffectsCommand(this);
        getCommand("keepeffects").setExecutor(cmd);
        getCommand("keepeffects").setTabCompleter(cmd);

        getLogger().info("KeepEffects enabled — v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("KeepEffects disabled.");
    }

    public static KeepEffectsPlugin getInstance() {
        return instance;
    }

    public EffectsConfig getEffectsConfig() {
        return effectsConfig;
    }

    public void reload() {
        reloadConfig();
        effectsConfig.load();
    }
}
