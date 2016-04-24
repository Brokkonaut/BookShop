package me.ibhh.BookShop;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import de.iani.playerUUIDCache.CachedPlayer;

public class BookShopListener implements Listener {

    private final BookShop plugin;
    private HashMap<UUID, Chest> chestViewers = new HashMap<UUID, Chest>();

    private static final BlockFace[] DOUBLE_CHEST_FACES = { BlockFace.SELF, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };

    public BookShopListener(BookShop BookShop) {
        this.plugin = BookShop;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void invClose(InventoryCloseEvent event) {
        Chest chest = BookShopListener.this.chestViewers.remove(event.getPlayer().getUniqueId());
        if (chest != null) {
            Inventory blockInventory = chest.getBlockInventory();
            Block chestblock = chest.getBlock();
            if (blockInventory != null && isSign(chestblock.getRelative(BlockFace.UP))) {
                Sign sign = (Sign) chestblock.getRelative(BlockFace.UP).getState();
                if (sign.getLine(0).equalsIgnoreCase(plugin.config.getFirstLineOfEveryShop())) {
                    int slot = blockInventory.first(Material.WRITTEN_BOOK);
                    if (slot >= 0) {
                        ItemStack item = blockInventory.getItem(slot);
                        BookMeta bm = (BookMeta) item.getItemMeta();
                        String title = ChatColor.stripColor(bm.getTitle());
                        if (title.length() > 15) {
                            title = title.substring(0, 15);
                        }
                        sign.setLine(2, title);
                        sign.update();
                    } else {
                        sign.setLine(2, "");
                        sign.update();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void invClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!this.chestViewers.containsKey(player.getUniqueId())) {
            return;
        }

        if (event.getInventory().getType().equals(InventoryType.CHEST)) {
            ItemStack bookItem = event.getCurrentItem();
            if (bookItem == null) {
                return;
            }

            if (bookItem.getType() == Material.WRITTEN_BOOK) {
                BookMeta bm = (BookMeta) bookItem.getItemMeta();
                if (!bm.getAuthor().equalsIgnoreCase(player.getName()) && !this.plugin.getPermissionsHandler().checkPermissionSilent(player, "BookShop.sell.other")) {
                    this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.onlyyourbooks." + this.plugin.getConfig().getString("language")), "Error");
                    event.setCancelled(true);
                    return;
                }
            }

            if (bookItem.getType() != Material.WRITTEN_BOOK && bookItem.getType() != Material.AIR) {
                if (!this.plugin.getConfig().getBoolean("useBookandQuill") || bookItem.getType() != Material.BOOK_AND_QUILL) {
                    this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.wrongItem." + this.plugin.config.language), "Error");
                    event.setCancelled(true);
                }
            } else if (((countWrittenBooks(this.chestViewers.get(player.getUniqueId()).getInventory()) <= 0) || (!this.chestViewers.get(player.getUniqueId()).getInventory().contains(event.getCurrentItem()))) && (countWrittenBooks(this.chestViewers.get(player.getUniqueId()).getInventory()) > 0)) {
                if (this.plugin.getConfig().getBoolean("useBookandQuill")) {
                    this.plugin.Logger("UseBooksandQuill = true", "Debug");
                    if ((!event.getCursor().getType().equals(Material.BOOK_AND_QUILL)) && (!event.getCurrentItem().getType().equals(Material.BOOK_AND_QUILL))) {
                        this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.wrongItem." + this.plugin.config.language), "Error");
                        event.setCancelled(true);
                    }
                } else {
                    this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.onebook." + this.plugin.config.language), "Error");
                    event.setCancelled(true);
                }
            } else if ((event.isShiftClick()) && (countWrittenBooks(this.chestViewers.get(player.getUniqueId()).getInventory()) > 0)) {
                this.plugin.PlayerLogger(player, this.plugin.getConfig().getString("Shop.error.onebook." + this.plugin.config.language), "Error");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String[] line = event.getLines();
        if (!line[0].equalsIgnoreCase(this.plugin.config.getFirstLineOfEveryShop())) {
            return; // no bookshop
        }
        Player p = event.getPlayer();
        if (!isPriceLineValid(line)) {
            plugin.PlayerLogger(p, "BookShop creation failed!", "Error");
            event.setCancelled(true);
            return;
        }

        Block chestblock = event.getBlock().getRelative(BlockFace.DOWN);
        if (!isChest(chestblock)) {
            plugin.PlayerLogger(p, plugin.getConfig().getString("Shop.error.nochest." + plugin.config.language), "Error");
            event.setCancelled(true);
            return;
        }
        Chest chest = (Chest) chestblock.getState();
        if (!isEmpty(chest.getInventory())) {
            plugin.PlayerLogger(p, plugin.getConfig().getString("Shop.error.nochest." + plugin.config.language), "Error");
            event.setCancelled(true);
            return;
        }

        if (event.getLine(1).equalsIgnoreCase(plugin.config.getAdminShopName())) {
            if (!plugin.getPermissionsHandler().checkPermission(p, "BookShop.create.admin")) {
                plugin.PlayerLogger(event.getPlayer(), "BookShop creation failed!", "Error");
                event.setCancelled(true);
                return;
            }
            event.setLine(1, plugin.config.getAdminShopName());
        } else {
            if (!plugin.getPermissionsHandler().checkPermission(p, "BookShop.create")) {
                plugin.PlayerLogger(event.getPlayer(), "BookShop creation failed!", "Error");
                event.setCancelled(true);
                return;
            }
            if (event.getLine(1).equalsIgnoreCase(event.getPlayer().getName()) || event.getLine(1).equalsIgnoreCase("")) {
                String playername = plugin.getNameShortener().getShortName(p.getUniqueId(), true);
                if (playername.equalsIgnoreCase(plugin.config.getAdminShopName())) {
                    plugin.PlayerLogger(event.getPlayer(), "Invalid Name!", "Error");
                    event.setCancelled(true);
                    return;
                }
                event.setLine(1, playername);
            } else if (!plugin.getPermissionsHandler().checkPermission(p, "BookShop.create.other")) {
                plugin.PlayerLogger(event.getPlayer(), "BookShop creation failed!", "Error");
                event.setCancelled(true);
                return;
            } else {
                // admin & name given
                CachedPlayer owner = plugin.getPlayerUUIDCache().getPlayer(event.getLine(1));
                if (owner == null) {
                    plugin.PlayerLogger(event.getPlayer(), "BookShop creation failed, unknown player!", "Error");
                    event.setCancelled(true);
                    return;
                }
                String playername = plugin.getNameShortener().getShortName(owner.getUUID(), true);
                event.setLine(1, playername);
            }
        }

        // if (plugin.getConfig().getBoolean("ShopCreateMessage")) {
        // plugin.getServer().broadcast("The player " + p.getName() + " created a BookShop: " + p.getLocation(), "BookShop.admin");
        // }
        if (plugin.getConfig().getBoolean("useBookandQuill")) {
            plugin.PlayerLogger(p, plugin.getConfig().getString("Shop.success.books." + plugin.config.language), "Warning");
        }
        plugin.PlayerLogger(event.getPlayer(), plugin.getConfig().getString("Shop.success.create." + plugin.config.language), "");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (isSign(event.getBlock())) {
            Sign s = (Sign) event.getBlock().getState();
            String[] line = s.getLines();
            if (line[0].equalsIgnoreCase(this.plugin.config.getFirstLineOfEveryShop()) && isPriceLineValid(line)) {
                if (!s.getLine(1).equalsIgnoreCase(plugin.getNameShortener().getShortName(p.getUniqueId(), false)) && !this.plugin.getPermissionsHandler().checkPermission(p, "BookShop.create.admin")) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (event.hasBlock() && !p.isSneaking()) {
                Block eventblock = event.getClickedBlock();
                if (isSign(eventblock)) {
                    Sign s = (Sign) event.getClickedBlock().getState();
                    String[] line = s.getLines();
                    if (line[0].equalsIgnoreCase(plugin.config.getFirstLineOfEveryShop())) {
                        LinksKlick(event, line, p, s);
                    }
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasBlock()) {
                if (isChest(event.getClickedBlock())) {
                    int check = isProtectedChest(event.getClickedBlock(), p);
                    if (check == -1) {
                        plugin.PlayerLogger(p, plugin.getConfig().getString("Shop.error.notyourshop." + plugin.config.language), "Warning");
                        event.setCancelled(true);
                    } else if (check == 1) {
                        chestViewers.put(p.getUniqueId(), (Chest) event.getClickedBlock().getState());
                    }
                }
            }
        }
    }

    private double getPrice(org.bukkit.block.Sign s, Player p, boolean BookandQuill) {
        return getPrice(s.getLine(3), p, BookandQuill);
    }

    private double getPrice(String s, Player p, boolean BookandQuill) {
        double doubleline3 = 0.0D;
        try {
            doubleline3 = Double.parseDouble(s);
        } catch (Exception e) {
            try {
                String[] a = s.split(":");
                double b0 = Double.parseDouble(a[0]);
                double b1 = Double.parseDouble(a[1]);
                if (BookandQuill) {
                    doubleline3 = b1;
                } else {
                    doubleline3 = b0;
                }
            } catch (Exception e1) {
                if (this.plugin.getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                }
            }
        }
        return doubleline3;
    }

    private boolean isPriceLineValid(String[] lines) {
        double temp = 0.0D;
        double b0 = 0.0D;
        double b1 = 0.0D;
        try {
            temp = Double.parseDouble(lines[3]);
        } catch (Exception e) {
            try {
                String[] a1 = lines[3].split(":");
                if (a1.length != 2) {
                    return false;
                }
                b0 = Double.parseDouble(a1[0]);
                b1 = Double.parseDouble(a1[1]);
            } catch (Exception e1) {
                return false;
            }
        }
        return (temp > 0.0D || (b1 > 0.0D) && b0 >= b1);
    }

    private boolean isChest(Block block) {
        return block.getType() == Material.CHEST;
    }

    private boolean isSign(Block block) {
        Material type = block.getType();
        return type == Material.SIGN_POST || type == Material.WALL_SIGN;
    }

    private int isProtectedChest(Block block, Player player) {
        boolean found = false;
        for (BlockFace bf : DOUBLE_CHEST_FACES) {
            Block faceBlock = block.getRelative(bf);
            if (isChest(faceBlock)) {
                int rv = isThisBlockProtectedChest(faceBlock, player);
                if (rv == -1) {
                    return -1;
                } else if (rv == 1) {
                    found = true;
                }
            }
        }
        return found ? 1 : 0;
    }

    private int isThisBlockProtectedChest(Block block, Player player) {
        Block up = block.getRelative(BlockFace.UP);
        if (up != null && isSign(up)) {
            BlockState upState = up.getState();
            if (upState instanceof Sign) {
                Sign sign = (Sign) upState;
                if (sign.getLine(0).equalsIgnoreCase(plugin.config.getFirstLineOfEveryShop())) {
                    if (!sign.getLine(1).equalsIgnoreCase(plugin.config.getAdminShopName()) && sign.getLine(1).equalsIgnoreCase(plugin.getNameShortener().getShortName(player.getUniqueId(), false))) {
                        return 1;
                    } else {
                        if (plugin.getPermissionsHandler().checkPermission(player, "BookShop.admin")) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }
            }
        }
        return 0;
    }

    private boolean isEmpty(Inventory inv) {
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getAmount() > 0) {
                return false;
            }
        }
        return true;
    }

    private void LinksKlick(PlayerInteractEvent event, String[] line, Player p, Sign s) {
        if (isPriceLineValid(line)) {
            Player player = event.getPlayer();
            if (plugin.getPermissionsHandler().checkPermission(p, "BookShop.use")) {
                BookShopSignBuy(player, line, s);
            }
        }
    }

    private void BookShopSignBuy(Player player, String[] line, Sign s) {
        if (!plugin.getNameShortener().getShortName(player.getUniqueId(), false).equalsIgnoreCase(line[1])) {
            if (player.getInventory().firstEmpty() != -1) {
                if (line[1].equalsIgnoreCase(plugin.config.getAdminShopName())) {
                    Chest chest = null;
                    try {
                        chest = (Chest) s.getBlock().getRelative(BlockFace.DOWN).getState();
                    } catch (Exception e) {
                    }
                    if (chest != null) {
                        if (chest.getInventory().contains(Material.WRITTEN_BOOK)) {
                            int Slot = chest.getInventory().first(Material.WRITTEN_BOOK);
                            ItemStack item = chest.getInventory().getItem(Slot);
                            if (item != null) {
                                double price = 0;
                                price = getPrice(s, player, false);
                                if (price >= 0) {
                                    if ((plugin.getMoneyHandler().getBalance(player) - price) >= 0 && plugin.getMoneyHandler().subtractMoney(player, price)) {
                                        player.getInventory().addItem(item.clone());
                                        plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), s.getLine(1), price), "");
                                    } else {
                                        plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                    }
                                } else {
                                    plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.wrongPrice." + plugin.config.language), "Error");
                                }
                            } else {
                                plugin.PlayerLogger(player, "An unknown error occurred!", "Error");
                                return;
                            }
                        } else {
                            plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.nobook." + plugin.config.language), "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.nobook." + plugin.config.language), "Error");
                    }
                } else {
                    Chest chest = null;
                    try {
                        chest = (Chest) s.getBlock().getRelative(BlockFace.DOWN).getState();
                    } catch (Exception e) {
                    }
                    if (chest != null) {
                        if (chest.getInventory().contains(Material.WRITTEN_BOOK)) {
                            int Slot = chest.getInventory().first(Material.WRITTEN_BOOK);
                            ItemStack item = chest.getInventory().getItem(Slot);
                            if (item != null) {
                                if (plugin.getConfig().getBoolean("useBookandQuill") && countBookAndQuills(chest.getInventory()) == 0) {
                                    if (player.getInventory().getItemInMainHand().getType() != Material.BOOK_AND_QUILL) {
                                        plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.takeBookandQuill." + plugin.config.language), "Error");
                                        return;
                                    }
                                }
                                double price = 0;
                                if (player.getInventory().getItemInMainHand().getType() == Material.BOOK_AND_QUILL) {
                                    price = getPrice(s, player, true);
                                } else {
                                    price = getPrice(s, player, false);
                                }
                                if (price >= 0) {
                                    if ((plugin.getMoneyHandler().getBalance(player) - price) >= 0) {
                                        if (player.getInventory().getItemInMainHand().getType() == Material.BOOK_AND_QUILL) {
                                            player.getInventory().clear(player.getInventory().getHeldItemSlot());
                                            player.getInventory().addItem(item.clone());
                                        } else if (plugin.getConfig().getBoolean("useBookandQuill") && countBookAndQuills(chest.getInventory()) > 0) {
                                            int Slotbook = chest.getInventory().first(Material.BOOK_AND_QUILL);
                                            ItemStack itembook = chest.getInventory().getItem(Slotbook);
                                            if (itembook.getAmount() > 1) {
                                                itembook.setAmount(itembook.getAmount() - 1);
                                            } else {
                                                chest.getInventory().clear(Slotbook);
                                            }
                                            player.getInventory().addItem(item.clone());
                                        } else {
                                            if (!plugin.getConfig().getBoolean("useBookandQuill")) {
                                                player.getInventory().addItem(item.clone());
                                            } else {
                                                plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.takeBookandQuill." + plugin.config.language), "Error");
                                                return;
                                            }
                                        }
                                        plugin.getMoneyHandler().subtractMoney(player, price);
                                        UUID realname = plugin.getNameShortener().getUUID(line[1]);
                                        OfflinePlayer owner = realname != null ? plugin.getServer().getOfflinePlayer(realname) : null;
                                        CachedPlayer owner2 = owner == null ? plugin.getPlayerUUIDCache().getPlayer(line[1]) : plugin.getPlayerUUIDCache().getPlayer(owner.getUniqueId());

                                        String ownerName = owner2 != null ? owner2.getName() : (owner != null && owner.getName() != null ? owner.getName() : line[1]);
                                        if (owner != null) {
                                            plugin.getMoneyHandler().addMoney(owner, price);
                                        }
                                        plugin.PlayerLogger(player, String.format(plugin.config.Shopsuccessbuy, s.getLine(2), ownerName, price), "");
                                        player.saveData();
                                        if (plugin.getServer().getPlayer(realname) != null) {
                                            plugin.PlayerLogger(plugin.getServer().getPlayer(line[1]), String.format(plugin.config.Shopsuccesssellerbuy, s.getLine(2), realname, price), "");
                                        }
                                    } else {
                                        plugin.PlayerLogger(player, plugin.config.Shoperrornotenoughmoneyconsumer, "Error");
                                    }
                                } else {
                                    plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.wrongPrice." + plugin.config.language), "Error");
                                }
                            } else {
                                plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.nobook." + plugin.config.language), "Error");
                            }
                        } else {
                            plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.nobook." + plugin.config.language), "Error");
                        }
                    } else {
                        plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.nochest2." + plugin.config.language), "Error");
                    }
                }
            } else {
                plugin.PlayerLogger(player, plugin.getConfig().getString("Shop.error.inventoryfull." + plugin.config.language), "Error");
            }
        } else {
            plugin.PlayerLogger(player, "That is your Shop", "Error");
        }
    }

    private int countBookAndQuills(Inventory inv) {
        int a = 0;
        for (ItemStack i : inv.getContents()) {
            if (i != null && i.getType() == Material.BOOK_AND_QUILL) {
                a++;
            }
        }
        return a;
    }

    private int countWrittenBooks(Inventory inv) {
        int a = 0;
        for (ItemStack i : inv.getContents()) {
            if (i != null && i.getType() == Material.WRITTEN_BOOK) {
                a++;
            }
        }
        return a;
    }

}
