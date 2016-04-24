package me.ibhh.BookShop.Tools;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NameShortener {

    /**
     * This Class was written by Brokkonaut, so many thanks to him.
     */
    private final JavaPlugin plugin;
    private final File databaseFile;
    private final YamlConfiguration namesConfig;
    private final HashMap<UUID, String> realToShortNames;
    private final HashMap<String, UUID> shortToRealNames;
    public final static int NAME_LENGTH_MAX = 15;

    /**
     * Erzeugt einen neuen Namenskürzer, der die Namen in der Datei
     * <b>shortnames.yaml</b> im Pluginverzeichnis verwendet. Achtung: Es darf
     * nur eine Instanz pro Plugin hiervon erstellt werden.
     *
     * @param plugin
     *            das Plugin, das diesen Namenskürzer verwendet
     */
    public NameShortener(JavaPlugin plugin) {
        this(plugin, new File(plugin.getDataFolder(), "shortnames.yaml"));
    }

    /**
     * Erzeugt einen neuen Namenskürzer. Achtung: Es darf nur eine Instanz pro
     * Plugin hiervon erstellt werden.
     *
     * @param plugin
     *            das Plugin, das diesen Namenskürzer verwendet
     * @param databaseFile
     *            eine Datenbankdatei mit den Kurznamen
     */
    public NameShortener(JavaPlugin plugin, File databaseFile) {
        if (plugin.getServer() == null) {
            throw new IllegalArgumentException("plugin has no server");
        }
        this.plugin = plugin;
        this.databaseFile = databaseFile;
        realToShortNames = new HashMap<UUID, String>();
        shortToRealNames = new HashMap<String, UUID>();
        namesConfig = new YamlConfiguration();

        // namen laden wenn möglich
        if (databaseFile.exists()) {
            try {
                namesConfig.load(databaseFile);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Could not load short names database " + databaseFile, e);
            }
            boolean converting = false;
            for (String realName : namesConfig.getKeys(false)) {
                String shortName = namesConfig.getString(realName);
                UUID uuid = null;
                if (realName.length() <= 36) {
                    if (!converting) {
                        plugin.getLogger().info("Start converting Names to UUIDs - This could take some time...");
                        converting = true;
                    }
                    uuid = convertRealName(realName);
                    namesConfig.set(realName, null);
                    if (uuid != null) {
                        namesConfig.set(uuid.toString(), shortName);
                    }
                } else {
                    try {
                        uuid = UUID.fromString(realName);
                    } catch (IllegalArgumentException e) {
                        // ignore
                    }
                }
                if (uuid != null) {
                    realToShortNames.put(uuid, shortName);
                    shortToRealNames.put(shortName.toLowerCase(), uuid);
                }
            }
            if (converting) {
                try {
                    namesConfig.save(databaseFile);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not save short names database " + databaseFile, e);
                }
                plugin.getLogger().info("Completed converting Names to UUIDs.");
            }
        }
    }

    @SuppressWarnings("deprecation")
    private UUID convertRealName(String realName) {
        OfflinePlayer op = plugin.getServer().getOfflinePlayer(realName);
        return op != null ? op.getUniqueId() : null;
    }

    /**
     * Gibt für einen echten Minecraftnamen einen Namen zurück, der auf ein
     * Schild passt. Bei neuen angepassten Namen wird der Name in die Datenbank
     * eingetragen
     *
     * @param realName
     *            der echte Name des Spielers. Darf nicht null sein.
     * @return der Name, der auf dem Schild erscheinen soll. Ist niemals null.
     */
    public synchronized String getShortName(Player player, boolean addNewToDatabase) {
        UUID uuid = player.getUniqueId();
        // prüfen, ob bereits ein shortname existiert
        String shortName = realToShortNames.get(uuid);
        if (shortName != null) {
            return shortName;
        }

        // wir müssen einen passenden namen suchen
        String realName = player.getName();
        int counter = 0;
        while (true) {
            String countString = counter == 0 ? "" : Integer.toString(counter);
            shortName = (realName.length() > NAME_LENGTH_MAX - countString.length() ? realName.substring(0, NAME_LENGTH_MAX - countString.length()) : realName) + countString;
            String shortNameLower = shortName.toLowerCase();
            // namen, die schon kurznamen sind, sind ungültig
            if (shortToRealNames.containsKey(shortNameLower)) {
                counter += 1;
                continue;
            }
            if (addNewToDatabase) {
                // der name ist gültig: eintragen und speichern
                realToShortNames.put(uuid, shortName);
                shortToRealNames.put(shortNameLower, uuid);
                namesConfig.set(uuid.toString(), shortName);
                try {
                    namesConfig.save(databaseFile);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not save short names database " + databaseFile, e);
                }
            }
            return shortName;
        }
    }

    /**
     * Gibt für einen Namen auf einem Schild den echten Namen des SPielers
     * zurück.
     *
     * @param shortName
     *            der Name auf dem Schild. Darf nicht null sein.
     * @return der echte Name. Ist niemals null.
     */
    public synchronized UUID getUUID(String shortName) {
        return shortToRealNames.get(shortName.trim().toLowerCase());
    }
}
