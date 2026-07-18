package me.phantom.vanish.commands;

import me.phantom.vanish.PhantomVanish;
import me.phantom.vanish.utils.MessageUtil;
import org.bukkit.Bukkit;
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
 *     <li>{@code /v <player>} - vanish another player (requires permission)</li>
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
            default -> handlePlayerSubcommand(player, args[0]);
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

    private void handlePlayerSubcommand(Player player, String targetName) {
        if (!player.hasPermission("phantomvanish.vanish.other")) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessageNoPermission());
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            MessageUtil.sendMessage(player, "&c✗ Player not found: " + targetName);
            return;
        }

        if (target.equals(player)) {
            MessageUtil.sendMessage(player, "&c✗ You cannot vanish yourself this way. Use /v instead!");
            return;
        }

        boolean wasVanished = plugin.getVanishManager().isVanished(target);
        plugin.getVanishManager().toggleVanish(target);

        // Broadcast the action
        if (wasVanished) {
            MessageUtil.broadcastVanishActionMessage(player.getName(), target.getName(), false);
        } else {
            MessageUtil.broadcastVanishActionMessage(player.getName(), target.getName(), true);
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
            
            // Add subcommands
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(prefix)) {
                    matches.add(sub);
                }
            }
            
            // Add online player names
            if (sender instanceof Player player && player.hasPermission("phantomvanish.vanish.other")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(prefix)) {
                        matches.add(p.getName());
                    }
                }
            }
            
            return matches;
        }
        return List.of();
    }
}
