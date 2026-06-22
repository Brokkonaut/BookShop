package me.ibhh.BookShop;

import org.bukkit.configuration.file.FileConfiguration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ConfigHandler {
    private BookShop plugin;
    private String language;
    private String firstLineOfEveryShop;
    private String firstLineOfEveryShopColor;
    private String adminShopName;

    private String messagePrefix;
    private String messageColor;
    private Component messagePrefixComponent;
    private TextColor messageTextColor;
    private Component firstLineOfEveryShopComponent;
    private MessageFormatter messageFormatter;

    public ConfigHandler(BookShop pl) {
        plugin = pl;

        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();

        language = plugin.getConfig().getString("language", "en");

        FileConfiguration config = plugin.getConfig();

        String prefixColorCode = config.getString("PrefixColor");
        String textColorCode = config.getString("TextColor");
        TextColor prefixColor = MessageFormatter.legacyColor(prefixColorCode);
        messageTextColor = MessageFormatter.legacyColor(textColorCode);

        String prefix = "[" + config.getString("Prefix", "BookShop") + "] ";
        messagePrefixComponent = config.getBoolean("UsePrefix") ? MessageFormatter.text(prefix, prefixColor) : Component.empty();
        messagePrefix = config.getBoolean("UsePrefix") ? (MessageFormatter.legacyColorPrefix(prefixColorCode) + prefix) : "";
        messageColor = MessageFormatter.legacyColorPrefix(textColorCode);
        firstLineOfEveryShop = config.getString("FirstLineOfEveryShop", "[BookShop]");
        firstLineOfEveryShopComponent = Component.text(firstLineOfEveryShop, NamedTextColor.BLUE);
        firstLineOfEveryShopColor = "§9" + firstLineOfEveryShop;
        adminShopName = config.getString("AdminShop", "AdminShop");
        messageFormatter = new MessageFormatter(messagePrefixComponent, messageTextColor);
    }

    public String getFirstLineOfEveryShop() {
        return firstLineOfEveryShop;
    }

    public String getFirstLineOfEveryShopColor() {
        return firstLineOfEveryShopColor;
    }

    public boolean isFirstLineOfEveryShop(String text) {
        String normalizedText = MessageFormatter.stripLegacyFormatting(text);
        return firstLineOfEveryShop.equalsIgnoreCase(normalizedText) || firstLineOfEveryShopColor.equalsIgnoreCase(text);
    }

    public boolean isFirstLineOfEveryShop(Component text) {
        return isFirstLineOfEveryShop(MessageFormatter.plain(text));
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

    public Component getMessagePrefixComponent() {
        return messagePrefixComponent;
    }

    public TextColor getMessageTextColor() {
        return messageTextColor;
    }

    public Component getFirstLineOfEveryShopComponent() {
        return firstLineOfEveryShopComponent;
    }

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    public String getTranslatedString(String key) {
        return plugin.getConfig().getString(key + "." + language, key);
    }
}
