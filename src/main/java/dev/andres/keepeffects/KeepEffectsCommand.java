package dev.andres.keepeffects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeepEffectsCommand implements CommandExecutor, TabCompleter {

    private final KeepEffectsPlugin plugin;
    private static final List<String> SUBS = Arrays.asList("reload", "toggle", "status");

    public KeepEffectsCommand(KeepEffectsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        EffectsConfig cfg = plugin.getEffectsConfig();

        if (!sender.hasPermission("keepeffects.admin")) {
            sender.sendMessage(cfg.getPrefix() + "\u00A7cNo permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label, cfg);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(plugin.getEffectsConfig().getPrefix() + plugin.getEffectsConfig().getMsgReload());
            }
            case "toggle" -> {
                boolean newState = !cfg.isEnabled();
                cfg.setEnabled(newState);
                String msg = newState ? cfg.getMsgToggledOn() : cfg.getMsgToggledOff();
                sender.sendMessage(cfg.getPrefix() + msg);
            }
            case "status" -> {
                String msg = cfg.isEnabled() ? cfg.getMsgStatusOn() : cfg.getMsgStatusOff();
                sender.sendMessage(cfg.getPrefix() + msg);
            }
            default -> sendHelp(sender, label, cfg);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label, EffectsConfig cfg) {
        sender.sendMessage(cfg.getPrefix() + "\u00A77/" + label + " reload \u00A78- \u00A7fReload config");
        sender.sendMessage(cfg.getPrefix() + "\u00A77/" + label + " toggle \u00A78- \u00A7fToggle plugin on/off");
        sender.sendMessage(cfg.getPrefix() + "\u00A77/" + label + " status \u00A78- \u00A7fCheck current status");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return SUBS.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
