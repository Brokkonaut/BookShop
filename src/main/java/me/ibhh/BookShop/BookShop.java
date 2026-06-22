package me.ibhh.BookShop;

import de.iani.playerUUIDCache.PlayerUUIDCache;
import me.ibhh.BookShop.Tools.NameShortener;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BookShop extends JavaPlugin {
    private ConfigHandler config;
    private EconomyHandler moneyHandler;
    private NameShortener nameShortener;
    private PlayerUUIDCache playerUUIDCache;

    @Override
    public void onEnable() {
        this.playerUUIDCache = (PlayerUUIDCache) getServer().getPluginManager().getPlugin("PlayerUUIDCache");
        this.config = new ConfigHandler(this);
        this.moneyHandler = new EconomyHandler(this);
        this.nameShortener = new NameShortener(this);
        getServer().getPluginManager().registerEvents(new BookShopListener(this), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sendInfoMessage(sender, "Version: " + getPluginMeta().getVersion());
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

    public void sendInfoMessage(CommandSender sender, String message) {
        sendInfoMessage(sender, Component.text(message == null ? "" : message));
    }

    public void sendInfoMessage(CommandSender sender, Component message) {
        sender.sendMessage(config.getMessageFormatter().info(message));
    }

    public void sendErrorMessage(CommandSender sender, String message) {
        sendErrorMessage(sender, Component.text(message == null ? "" : message));
    }

    public void sendErrorMessage(CommandSender sender, Component message) {
        sender.sendMessage(config.getMessageFormatter().error(message));
    }

    public boolean checkPermission(Player player, String action) {
        if (player.hasPermission(action)) {
            return true;
        }
        sendErrorMessage(player, config.getTranslatedString("permissions.error") + " (" + action + ")");
        return false;
    }
}
