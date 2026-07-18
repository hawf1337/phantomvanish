package me.phantom.vanish.commands;

import me.phantom.vanish.PhantomVanish;
import me.phantom.vanish.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handles {@code /v} and {@code /vanish}, and their subcommands:
 * <ul>
 *     <li>{@code /v} - toggle vanish on/off</li>
 *     <li>{@code /v help} - show the help menu</li>
 *     <li>{@code /v item} - toggle item pickup blocking while vanished</li>
 *     <li>{@code /v list} - list players currently in vanish</li>
 * </ul>
 */
public class VanishCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("help", "item", "list");

    private final PhantomVanish plugin;

    public VanishCommand(PhantomVanish plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(stripColor(plugin.getConfigManager().getMessagePlayerOnly()));
            return true;
        }

        if (!player.hasPermission(plugin.getConfigManager().getPermissionVanish())) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageNoPermission());
            return true;
        }

        if (args.length == 0) {
            plugin.getVanishManager().toggleVanish(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help", "?" -> sendHelp(player);
            case "item" -> handleItemSubcommand(player);
            case "list" -> handleListSubcommand(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleItemSubcommand(Player player) {
        if (!player.hasPermission(plugin.getConfigManager().getPermissionVanishItem())) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageNoPermission());
            return;
        }
        plugin.getItemPickupManager().toggle(player);
    }

    private void handleListSubcommand(Player player) {
        if (!player.hasPermission(plugin.getConfigManager().getPermissionVanishSee())) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageNoPermission());
            return;
        }

        List<String> names = plugin.getVanishManager().getVanishedOnlinePlayerNames();
        if (names.isEmpty()) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageVanishListEmpty());
            return;
        }

        MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageVanishListHeader());
        String prefix = plugin.getConfigManager().getMessageVanishListEntryPrefix();
        for (String name : names) {
            MessageUtil.sendMessage(player, prefix + name);
        }
    }

    private void sendHelp(Player player) {
        for (String line : plugin.getConfigManager().getHelpMessages()) {
            MessageUtil.sendMessage(player, line);
        }
    }

    /**
     * Removes legacy '&' color codes for console/plain-text output, since
     * console senders don't render Adventure components.
     */
    private String stripColor(String legacyText) {
        return legacyText == null ? "" : legacyText.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> matches = new ArrayList<>();
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(prefix)) {
                    matches.add(sub);
                }
            }
            return matches;
        }
        return List.of();
    }
}
