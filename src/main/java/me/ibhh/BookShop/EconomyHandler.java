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
            throw new IllegalStateException("No economy plugin found!");
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
        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not get balance of player:" + player.getName() + " (" + player.getUniqueId() + ")", e);
        }
        return 0.0;
    }

    public String formatMoney(double amount) {
        return economy.format(amount);
    }

    public boolean subtractMoney(OfflinePlayer player, double amount) {
        try {
            EconomyResponse result = economy.withdrawPlayer(player, amount);
            return result != null && result.transactionSuccess();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not take money from player:" + player.getName() + " (" + player.getUniqueId() + ")", e);
        }
        return false;
    }

    public boolean addMoney(OfflinePlayer player, double amount) {
        try {
            EconomyResponse result = economy.depositPlayer(player, amount);
            return result != null && result.transactionSuccess();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not give money to player:" + player.getName() + " (" + player.getUniqueId() + ")", e);
        }
        return false;
    }
}