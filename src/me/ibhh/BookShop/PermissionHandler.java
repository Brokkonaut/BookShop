package me.ibhh.BookShop;

import org.bukkit.entity.Player;

public class PermissionHandler {

    private BookShop plugin;

    public PermissionHandler(BookShop pl) {
        this.plugin = pl;
    }

    public boolean checkPermissionSilent(Player player, String action) {
        return player.hasPermission(action);
    }

    public boolean checkPermission(Player player, String action) {
        if (player.hasPermission(action)) {
            return true;
        }
        plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
        return false;
    }
}