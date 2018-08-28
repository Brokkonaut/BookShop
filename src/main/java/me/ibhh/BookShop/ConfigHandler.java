package me.ibhh.BookShop;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigHandler {
    private BookShop plugin;
    private String language;
    private String firstLineOfEveryShop;
    private String firstLineOfEveryShopColor;
    private String adminShopName;

    private String messagePrefix;
    private String messageColor;

    public ConfigHandler(BookShop pl) {
        plugin = pl;

        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();

        language = plugin.getConfig().getString("language");

        FileConfiguration config = plugin.getConfig();

        ChatColor prefixColor = ChatColor.getByChar(config.getString("PrefixColor"));
        ChatColor textColor = ChatColor.getByChar(config.getString("TextColor"));

        messagePrefix = config.getBoolean("UsePrefix") ? ((prefixColor != null ? prefixColor.toString() : "") + "[" + config.getString("Prefix") + "] ") : "";
        messageColor = textColor == null ? "" : textColor.toString();
        firstLineOfEveryShop = config.getString("FirstLineOfEveryShop");
        firstLineOfEveryShopColor = ChatColor.BLUE + firstLineOfEveryShop;
        adminShopName = config.getString("AdminShop");
    }

    public String getFirstLineOfEveryShop() {
        return firstLineOfEveryShop;
    }

    public String getFirstLineOfEveryShopColor() {
        return firstLineOfEveryShopColor;
    }

    public boolean isFirstLineOfEveryShop(String text) {
        return firstLineOfEveryShop.equalsIgnoreCase(text) || firstLineOfEveryShopColor.equalsIgnoreCase(text);
    }

    public String getAdminShopName() {
        return adminShopName;
    }

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public String getMessageColor() {
        return messageColor;
    }

    public String getTranslatedString(String key) {
        return plugin.getConfig().getString(key + "." + language);
    }
}
