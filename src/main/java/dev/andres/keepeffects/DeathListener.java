package dev.andres.keepeffects;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class DeathListener implements Listener {

    private final KeepEffectsPlugin plugin;
    // Stores effects per player UUID between death and respawn
    private final Map<UUID, List<PotionEffect>> storedEffects = new HashMap<>();

    public DeathListener(KeepEffectsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        EffectsConfig cfg = plugin.getEffectsConfig();

        // Check global toggle
        if (!cfg.isEnabled() && !player.hasPermission("keepeffects.keep.bypass")) return;

        // Check permission
        if (!player.hasPermission("keepeffects.keep") && !player.hasPermission("keepeffects.keep.bypass")) return;

        // Check world
        if (!isWorldAllowed(player.getWorld(), cfg)) return;

        // Respect keepInventory gamerule unless always-apply is true
        if (!cfg.isAlwaysApply()) {
            Boolean keepInv = player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
            if (Boolean.TRUE.equals(keepInv)) return; // let vanilla handle it
        }

        Collection<PotionEffect> activeEffects = player.getActivePotionEffects();
        if (activeEffects.isEmpty()) return;

        List<PotionEffect> toKeep = new ArrayList<>();

        for (PotionEffect effect : activeEffects) {
            PotionEffectType type = effect.getType();

            // Whitelist mode: only keep whitelisted (if whitelist is non-empty)
            if (!cfg.getWhitelist().isEmpty()) {
                if (!cfg.getWhitelist().contains(type)) continue;
            } else {
                // Blacklist mode: skip blacklisted effects
                if (cfg.getBlacklist().contains(type)) continue;
            }

            // Apply duration reduction
            int duration = effect.getDuration();
            if (cfg.isReduceDuration()) {
                duration = (int) (duration * cfg.getDurationFactor());
                if (duration <= 0) continue; // expired, don't keep
            }

            // Apply amplifier reduction
            int amplifier = effect.getAmplifier();
            if (cfg.isReduceAmplifier()) {
                amplifier = amplifier - cfg.getAmplifierSubtract();
                if (amplifier < 0) continue; // removed
            }

            toKeep.add(new PotionEffect(type, duration, amplifier,
                    effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
        }

        if (!toKeep.isEmpty()) {
            storedEffects.put(player.getUniqueId(), toKeep);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        List<PotionEffect> effects = storedEffects.remove(uuid);
        if (effects == null || effects.isEmpty()) return;

        // Apply on next tick so bukkit doesn't wipe them
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            for (PotionEffect effect : effects) {
                player.addPotionEffect(effect);
            }

            EffectsConfig cfg = plugin.getEffectsConfig();
            String msg = cfg.getMsgKept();
            if (msg != null && !msg.isEmpty()) {
                player.sendMessage(cfg.getPrefix() + msg);
            }
        }, 1L);
    }

    private boolean isWorldAllowed(World world, EffectsConfig cfg) {
        List<String> worlds = cfg.getWorlds();
        if (worlds.contains("all")) return true;
        return worlds.stream().anyMatch(w -> w.equalsIgnoreCase(world.getName()));
    }
}
