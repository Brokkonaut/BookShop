package me.ibhh.BookShop;

import me.ibhh.BookShop.Tools.NameShortener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.iani.playerUUIDCache.PlayerUUIDCache;

public class BookShop extends JavaPlugin {
    private ConfigHandler config;
    private EconomyHandler moneyHandler;
    private NameShortener nameShortener;
    private PlayerUUIDCache playerUUIDCache;

    @Override
    public void onEnable() {
        this.playerUUIDCache = (PlayerUUIDCache) getServer().getPluginManager().getPlugin("PlayerUUIDCache");
        this.config = new ConfigHandler(this);
        this.nameShortener = new NameShortener(this);
        this.moneyHandler = new EconomyHandler(this);
        getServer().getPluginManager().registerEvents(new BookShopListener(this), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sendInfoMessage(sender, "Version: " + getDescription().getVersion());
        return true;
    }

    public PlayerUUIDCache getPlayerUUIDCache() {
        return playerUUIDCache;
    }

    public NameShortener getNameShortener() {
        return nameShortener;
    }

    public EconomyHandler getEconomyHandler() {
        return moneyHandler;
    }

    public ConfigHandler getConfigHandler() {
        return config;
    }

    public void sendInfoMessage(CommandSender p, String msg) {
        p.sendMessage(config.getMessagePrefix() + config.getMessageColor() + msg);
    }

    public void sendErrorMessage(CommandSender p, String msg) {
        p.sendMessage(config.getMessagePrefix() + ChatColor.RED + "ERROR: " + config.getMessageColor() + msg);
    }

    public boolean checkPermission(Player player, String action) {
        if (player.hasPermission(action)) {
            return true;
        }
        sendErrorMessage(player, config.getTranslatedString("permissions.error") + " (" + action + ")");
        return false;
    }
}