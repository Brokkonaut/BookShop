package me.ibhh.BookShop;

import me.ibhh.BookShop.Tools.NameShortener;
import me.ibhh.BookShop.logger.LoggerUtility;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BookShop extends JavaPlugin {
    private LoggerUtility logger;
    public ConfigHandler config;
    public PermissionsChecker PermissionsHandler;
    private EconomyHandler MoneyHandler;
    private NameShortener nameShortener;

    @Override
    public void onEnable() {
        this.logger = new LoggerUtility(this);
        this.config = new ConfigHandler(this);
        this.nameShortener = new NameShortener(this);
        this.MoneyHandler = new EconomyHandler(this);
        this.PermissionsHandler = new PermissionsChecker(this);
        getServer().getPluginManager().registerEvents(new BookShopListener(this), this);
    }

    public LoggerUtility getLoggerUtility() {
        return logger;
    }

    public NameShortener getNameShortener() {
        return nameShortener;
    }

    public EconomyHandler getMoneyHandler() {
        return MoneyHandler;
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