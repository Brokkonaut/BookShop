/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ibhh.BookShop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Simon
 */
public class ConfigHandler {
    // define globale variables

    private BookShop plugin;
    public String language, Shopsuccessbuy, Shopsuccesssellerbuy, Shoperrornotenoughmoneyconsumer;
    private String firstLineOfEveryShop;
    private String adminShopName;

    /**
     * Konstruktor
     *
     * @param pl
     * @throws IOException
     */
    public ConfigHandler(BookShop pl) {
        plugin = pl;

        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();
        reload();

        File configurationFile = new File(plugin.getDataFolder().toString() + File.separator + "Shopconfig.yml");
        YamlConfiguration SHOP_configuration = YamlConfiguration.loadConfiguration(configurationFile);
        SHOP_configuration.addDefault("FirstLineOfEveryShop", "[BookShop]");
        SHOP_configuration.addDefault("AdminShop", "AdminShop");
        SHOP_configuration.options().copyDefaults(true);
        try {
            SHOP_configuration.save(configurationFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save Shopconfig.yml", e);
        }
        firstLineOfEveryShop = SHOP_configuration.getString("FirstLineOfEveryShop");
        adminShopName = SHOP_configuration.getString("AdminShop");

    }

    public String getFirstLineOfEveryShop() {
        return firstLineOfEveryShop;
    }

    public String getAdminShopName() {
        return adminShopName;
    }

    /**
     * loadsConfig
     */
    private void reload() {
        language = plugin.getConfig().getString("language");
        Shoperrornotenoughmoneyconsumer = plugin.getConfig().getString("Shop.error.notenoughmoneyconsumer." + language);
        Shopsuccessbuy = plugin.getConfig().getString("Shop.success.buy." + language);
        Shopsuccesssellerbuy = plugin.getConfig().getString("Shop.success.sellerbuy." + language);
    }
}
