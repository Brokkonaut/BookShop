package me.ibhh.BookShop.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import me.ibhh.BookShop.BookShop;

import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.playerUUIDCache.CachedPlayer;

public class NameShortener {

    /**
     * This Class was written by Brokkonaut, so many thanks to him.
     */
    private final BookShop plugin;
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
    public NameShortener(BookShop plugin) {
        this(plugin, new File(plugin.getDataFolder(), "shortnames.yml"));
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
    public NameShortener(BookShop plugin, File databaseFile) {
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
            HashMap<String, String> converting = null;
            for (String realName : namesConfig.getKeys(false)) {
                String shortName = namesConfig.getString(realName);
                UUID uuid = null;
                if (realName.length() < 36) {
                    if (converting == null) {
                        converting = new HashMap<String, String>();
                    }
                    converting.put(realName.toLowerCase(), shortName);
                    namesConfig.set(realName, null);
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
            if (converting != null) {
                plugin.getLogger().info("Start converting Names to UUIDs - This could take some time...");
                Collection<CachedPlayer> playersFound = plugin.getPlayerUUIDCache().getPlayers(new ArrayList<String>(converting.keySet()), true);
                for (CachedPlayer cp : playersFound) {
                    String shortName = converting.get(cp.getName().toLowerCase());
                    if (shortName != null) {
                        realToShortNames.put(cp.getUUID(), shortName);
                        shortToRealNames.put(shortName.toLowerCase(), cp.getUUID());
                        namesConfig.set(cp.getUUID().toString(), shortName);
                    }
                }

                try {
                    namesConfig.save(databaseFile);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Could not save short names database " + databaseFile, e);
                }
                plugin.getLogger().info("Completed converting Names to UUIDs.");
            }
        }
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
    public synchronized String getShortName(UUID uuid, boolean addNewToDatabase) {
        // prüfen, ob bereits ein shortname existiert
        String shortName = realToShortNames.get(uuid);
        if (shortName != null) {
            return shortName;
        }

        // wir müssen einen passenden namen suchen
        CachedPlayer cp = plugin.getPlayerUUIDCache().getPlayer(uuid, true);
        if (cp == null) {
            throw new IllegalArgumentException("User unknown!");
        }
        String realName = cp.getName();
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
