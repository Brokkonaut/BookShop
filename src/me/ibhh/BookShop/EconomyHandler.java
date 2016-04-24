package me.ibhh.BookShop;

import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHandler {
    private BookShop plugin;
    private Economy economy;

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

    public double getBalance(OfflinePlayer player) {
        if (economy == null) {
            return 0.0;
        }
        return economy.getBalance(player);
    }

    public boolean subtractMoney(OfflinePlayer player, double amount) {
        if (economy == null) {
            return false;
        }
        EconomyResponse result = economy.withdrawPlayer(player, amount);
        return result != null && result.transactionSuccess();
    }

    public void addMoney(OfflinePlayer player, double amount) {
        try {
            economy.depositPlayer(player, amount);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Cant add money! Does account exist? :" + player.getName(), e);
        }
    }
}