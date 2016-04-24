package me.ibhh.BookShop;

import me.ibhh.BookShop.Tools.NameShortener;
import me.ibhh.BookShop.logger.LoggerUtility;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.iani.playerUUIDCache.PlayerUUIDCache;

public class BookShop extends JavaPlugin {
    private LoggerUtility logger;
    public ConfigHandler config;
    private PermissionHandler permissionsHandler;
    private EconomyHandler moneyHandler;
    private NameShortener nameShortener;
    private PlayerUUIDCache playerUUIDCache;

    @Override
    public void onEnable() {
        this.logger = new LoggerUtility(this);
        this.playerUUIDCache = (PlayerUUIDCache) getServer().getPluginManager().getPlugin("PlayerUUIDCache");
        this.config = new ConfigHandler(this);
        this.nameShortener = new NameShortener(this);
        this.moneyHandler = new EconomyHandler(this);
        this.permissionsHandler = new PermissionHandler(this);
        getServer().getPluginManager().registerEvents(new BookShopListener(this), this);
    }

    public LoggerUtility getLoggerUtility() {
        return logger;
    }

    public PlayerUUIDCache getPlayerUUIDCache() {
        return playerUUIDCache;
    }

    public NameShortener getNameShortener() {
        return nameShortener;
    }

    public EconomyHandler getMoneyHandler() {
        return moneyHandler;
    }

    public PermissionHandler getPermissionsHandler() {
        return permissionsHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        PlayerLogger((Player) sender, "Version: " + getDescription().getVersion(), "");
        return true;
    }

    public void Logger(String msg, String TYPE) {
        try {
            if ((TYPE.equalsIgnoreCase("Warning"))) {
                getLoggerUtility().log(msg, LoggerUtility.Level.WARNING);
            } else if (TYPE.equalsIgnoreCase("Debug")) {
                getLoggerUtility().log(msg, LoggerUtility.Level.DEBUG);
            } else if ((TYPE.equalsIgnoreCase("Error"))) {
                getLoggerUtility().log(msg, LoggerUtility.Level.WARNING);
            } else {
                getLoggerUtility().log(msg, LoggerUtility.Level.INFO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[BookShop] Error: Uncatch Exeption!");
        }
    }

    public void PlayerLogger(Player p, String msg, String TYPE) {
        try {
            if (TYPE.equalsIgnoreCase("Error")) {
                getLoggerUtility().log(p, msg, LoggerUtility.Level.ERROR);
            } else if (TYPE.equalsIgnoreCase("Warning")) {
                getLoggerUtility().log(p, msg, LoggerUtility.Level.WARNING);
            } else {
                getLoggerUtility().log(p, msg, LoggerUtility.Level.INFO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[BookShop] Error: Uncatch Exeption!");
        }
    }
}