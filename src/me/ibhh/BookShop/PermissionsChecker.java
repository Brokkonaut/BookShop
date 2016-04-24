package me.ibhh.BookShop;

import org.bukkit.entity.Player;

public class PermissionsChecker {

    private BookShop plugin;

    public PermissionsChecker(BookShop pl) {
        this.plugin = pl;
    }

    public boolean checkpermissionssilent(Player player, String action) {
        if (player.isOp()) {
            return true;
        }
        return player.hasPermission(action);
    }

    public boolean checkpermissions(Player player, String action) {
        if (player.isOp()) {
            return true;
        }
        if (player.hasPermission(action)) {
            return true;
        } else {
            plugin.PlayerLogger(player, player.getName() + " " + plugin.getConfig().getString("permissions.error." + plugin.getConfig().getString("language")) + " (" + action + ")", "Error");
            return false;
        }
    }
}