package dev.andres.keepeffects;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EffectsConfig {

    private final KeepEffectsPlugin plugin;

    private boolean enabled;
    private boolean alwaysApply;
    private List<String> worlds;

    private boolean reduceDuration;
    private double durationFactor;

    private boolean reduceAmplifier;
    private int amplifierSubtract;

    private Set<PotionEffectType> blacklist;
    private Set<PotionEffectType> whitelist;

    // Messages
    private String prefix;
    private String msgKept;
    private String msgNoneKept;
    private String msgReload;
    private String msgToggledOn;
    private String msgToggledOff;
    private String msgStatusOn;
    private String msgStatusOff;

    public EffectsConfig(KeepEffectsPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfig();

        enabled = cfg.getBoolean("enabled", true);
        alwaysApply = cfg.getBoolean("always-apply", true);
        worlds = cfg.getStringList("worlds");
        if (worlds.isEmpty()) worlds = List.of("all");

        reduceDuration = cfg.getBoolean("reduce-duration.enabled", false);
        durationFactor = cfg.getDouble("reduce-duration.factor", 0.5);

        reduceAmplifier = cfg.getBoolean("reduce-amplifier.enabled", false);
        amplifierSubtract = cfg.getInt("reduce-amplifier.subtract", 1);

        blacklist = parseEffectList(cfg.getStringList("blacklisted-effects"));
        whitelist = parseEffectList(cfg.getStringList("whitelisted-effects"));

        prefix     = color(cfg.getString("messages.prefix", "&8[&bKeepEffects&8] "));
        msgKept        = color(cfg.getString("messages.kept", "&aYour potion effects were preserved after death!"));
        msgNoneKept    = color(cfg.getString("messages.none-kept", ""));
        msgReload      = color(cfg.getString("messages.reload", "&aConfiguration reloaded."));
        msgToggledOn   = color(cfg.getString("messages.toggled-on", "&aKeepEffects globally &2enabled&a."));
        msgToggledOff  = color(cfg.getString("messages.toggled-off", "&cKeepEffects globally &4disabled&c."));
        msgStatusOn    = color(cfg.getString("messages.status-on", "&aKeepEffects is currently &2ENABLED&a."));
        msgStatusOff   = color(cfg.getString("messages.status-off", "&cKeepEffects is currently &4DISABLED&c."));
    }

    private Set<PotionEffectType> parseEffectList(List<String> names) {
        Set<PotionEffectType> set = new HashSet<>();
        for (String name : names) {
            PotionEffectType type = PotionEffectType.getByName(name.toUpperCase());
            if (type != null) {
                set.add(type);
            } else {
                plugin.getLogger().warning("Unknown potion effect in config: " + name);
            }
        }
        return set;
    }

    private String color(String s) {
        if (s == null) return "";
        return s.replace("&", "\u00A7");
    }

    // ---- Getters ----

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean v) { enabled = v; }

    public boolean isAlwaysApply() { return alwaysApply; }

    public List<String> getWorlds() { return worlds; }

    public boolean isReduceDuration() { return reduceDuration; }
    public double getDurationFactor() { return durationFactor; }

    public boolean isReduceAmplifier() { return reduceAmplifier; }
    public int getAmplifierSubtract() { return amplifierSubtract; }

    public Set<PotionEffectType> getBlacklist() { return blacklist; }
    public Set<PotionEffectType> getWhitelist() { return whitelist; }

    public String getPrefix()       { return prefix; }
    public String getMsgKept()      { return msgKept; }
    public String getMsgNoneKept()  { return msgNoneKept; }
    public String getMsgReload()    { return msgReload; }
    public String getMsgToggledOn() { return msgToggledOn; }
    public String getMsgToggledOff(){ return msgToggledOff; }
    public String getMsgStatusOn()  { return msgStatusOn; }
    public String getMsgStatusOff() { return msgStatusOff; }
}
