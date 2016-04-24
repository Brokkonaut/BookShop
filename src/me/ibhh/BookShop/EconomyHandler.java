package me.ibhh.BookShop;

import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHandler {
    private BookShop plugin;
    public static Economy economy = null;

    public EconomyHandler(BookShop pl) {
        plugin = pl;
        if (!setupEconomy()) {
            plugin.getLogger().severe("No economy plugin found!");
        }
    }

    private boolean setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
        } catch (NoClassDefFoundError e) {
            return false;
        }
        return economy != null;
    }

    public double getBalance(Player player) {
        if (economy == null) {
            return 0.0;
        }
        return economy.getBalance(player);
    }

    public boolean subtract(double amountsubtract, Player player) {
        if (economy == null) {
            return false;
        }
        EconomyResponse result = economy.withdrawPlayer(player, amountsubtract);
        return result != null && result.transactionSuccess();
    }

    public void addmoney(double amountadd, OfflinePlayer player) {
        try {
            economy.depositPlayer(player, amountadd);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cant add money! Does account exist? :" + player.getName(), e);
        }
    }
}