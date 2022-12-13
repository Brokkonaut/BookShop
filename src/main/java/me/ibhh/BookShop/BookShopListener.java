package me.ibhh.BookShop;

import de.iani.playerUUIDCache.CachedPlayer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class BookShopListener implements Listener {
    private enum ChestProtectionState {
        OWN_CHEST,
        FOREIGN_CHEST,
        NO_BOOKSHOP_CHEST
    }

    private final BookShop plugin;
    private final HashSet<UUID> chestViewers;

    public BookShopListener(BookShop BookShop) {
        this.plugin = BookShop;
        this.chestViewers = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (BookShopListener.this.chestViewers.remove(event.getPlayer().getUniqueId())) {
            Block chestblock = null;
            if (event.getInventory() instanceof DoubleChestInventory dc) {
                Block block = dc.getLeftSide().getLocation().getBlock();
                if (isThisBlockProtectedChest(block, event.getPlayer()) == ChestProtectionState.OWN_CHEST) {
                    chestblock = block;
                } else {
                    block = dc.getRightSide().getLocation().getBlock();
                    if (isThisBlockProtectedChest(block, event.getPlayer()) == ChestProtectionState.OWN_CHEST) {
                        chestblock = block;
                    }
                }
            } else {
                Location loc = event.getInventory().getLocation();
                chestblock = loc == null ? null : loc.getBlock();
            }
            if (chestblock == null) {
                return;
            }
            Inventory blockInventory = event.getInventory();
            Block signBlock = chestblock.getRelative(BlockFace.UP);
            if (blockInventory != null && isSign(signBlock)) {
                Sign sign = (Sign) signBlock.getState();
                if (plugin.getConfigHandler().isFirstLineOfEveryShop(sign.getLine(0))) {
                    String title = "";
                    int slot = blockInventory.first(Material.PAPER);
                    if (slot >= 0) {
                        ItemStack item = blockInventory.getItem(slot);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null && meta.hasDisplayName()) {
                            title = meta.getDisplayName();
                        }
                    }
                    if (title.equals("")) {
                        slot = blockInventory.first(Material.WRITTEN_BOOK);
                        if (slot >= 0) {
                            ItemStack item = blockInventory.getItem(slot);
                            BookMeta bm = (BookMeta) item.getItemMeta();
                            title = ChatColor.stripColor(bm.getTitle());
                            if (title.length() > 15) {
                                title = title.substring(0, 15);
                            }
                        }
                    }
                    sign.setLine(0, plugin.getConfigHandler().getFirstLineOfEveryShopColor());
                    sign.setLine(2, title);
                    sign.update();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!this.chestViewers.contains(player.getUniqueId())) {
            return;
        }

        ItemStack bookItem = event.getCurrentItem();

        // ablegen und rausnehmen aus der kiste geht immer
        if (bookItem == null || bookItem.getType() == Material.AIR || event.getClickedInventory() == event.getInventory()) {
            return;
        }

        if (bookItem.getType() == Material.WRITTEN_BOOK) {
            BookMeta bm = (BookMeta) bookItem.getItemMeta();
            if (!bm.getAuthor().equalsIgnoreCase(player.getName()) && !player.hasPermission("bookshop.sellother")) {
                this.plugin.sendErrorMessage(player, plugin.getConfigHandler().getTranslatedString("Shop.error.onlyyourbooks"));
                event.setCancelled(true);
                return;
            }
        }

        if (bookItem.getType() == Material.WRITTEN_BOOK) {
            int slot = event.getInventory().first(Material.WRITTEN_BOOK);
            if (slot >= 0) {
                ItemStack existingBook = event.getInventory().getItem(slot);
                if (!existingBook.isSimilar(bookItem)) {
                    this.plugin.sendErrorMessage(player, plugin.getConfigHandler().getTranslatedString("Shop.error.onebook"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String[] line = event.getLines();
        if (!plugin.getConfigHandler().isFirstLineOfEveryShop(line[0])) {
            return;
        }
        Player p = event.getPlayer();
        if (!isPriceLineValid(line)) {
            plugin.sendErrorMessage(p, plugin.getConfigHandler().getTranslatedString("Shop.error.wrongPrice"));
            event.setCancelled(true);
            return;
        }

        Block chestblock = event.getBlock().getRelative(BlockFace.DOWN);
        if (!isChest(chestblock)) {
            plugin.sendErrorMessage(p, plugin.getConfigHandler().getTranslatedString("Shop.error.nochest2"));
            event.setCancelled(true);
            return;
        }

        String playerName;
        if (event.getLine(1).equalsIgnoreCase(plugin.getConfigHandler().getAdminShopName())) {
            if (!plugin.checkPermission(p, "bookshop.admin")) {
                event.setCancelled(true);
                return;
            }
            playerName = plugin.getConfigHandler().getAdminShopName();
        } else {
            if (!plugin.checkPermission(p, "bookshop.create")) {
                event.setCancelled(true);
                return;
            }
            if (event.getLine(1).equalsIgnoreCase(event.getPlayer().getName()) || event.getLine(1).equalsIgnoreCase("")) {
                playerName = plugin.getNameShortener().getShortName(p.getUniqueId(), true);
                if (playerName.equalsIgnoreCase(plugin.getConfigHandler().getAdminShopName())) {
                    plugin.sendErrorMessage(event.getPlayer(), "Invalid Name!");
                    event.setCancelled(true);
                    return;
                }
            } else if (!plugin.checkPermission(p, "bookshop.admin")) {
                event.setCancelled(true);
                return;
            } else {
                // admin & name given
                CachedPlayer owner = plugin.getPlayerUUIDCache().getPlayer(event.getLine(1));
                if (owner == null) {
                    plugin.sendErrorMessage(event.getPlayer(), "BookShop creation failed, unknown player!");
                    event.setCancelled(true);
                    return;
                }
                playerName = plugin.getNameShortener().getShortName(owner.getUUID(), true);
            }
        }
        event.setLine(0, plugin.getConfigHandler().getFirstLineOfEveryShopColor());
        event.setLine(1, playerName);

        plugin.getLogger().info("The player " + p.getName() + " created a BookShop: " + p.getLocation());
        plugin.sendInfoMessage(event.getPlayer(), plugin.getConfigHandler().getTranslatedString("Shop.success.create"));
        plugin.sendInfoMessage(p, plugin.getConfigHandler().getTranslatedString("Shop.success.books"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (isSign(event.getBlock())) {
            Sign s = (Sign) event.getBlock().getState();
            String[] line = s.getLines();
            if (plugin.getConfigHandler().isFirstLineOfEveryShop(line[0]) && isPriceLineValid(line)) {
                if (!s.getLine(1).equalsIgnoreCase(plugin.getNameShortener().getShortName(p.getUniqueId(), false)) && !this.plugin.checkPermission(p, "BookShop.admin")) {
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
                    Sign s = (Sign) eventblock.getState();
                    String[] line = s.getLines();
                    if (plugin.getConfigHandler().isFirstLineOfEveryShop(line[0])) {
                        buyFromShop(p, line, s);
                    }
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasBlock() && isProtectedChest(event.getClickedBlock(), p) == ChestProtectionState.FOREIGN_CHEST) {
                plugin.sendErrorMessage(p, plugin.getConfigHandler().getTranslatedString("Shop.error.notyourshop"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteractMonitor(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.hasBlock() && isProtectedChest(event.getClickedBlock(), p) == ChestProtectionState.OWN_CHEST) {
                chestViewers.add(p.getUniqueId());
            }
        }
    }

    private double getPrice(String[] signText, Player p, boolean bookInHand) {
        String s = signText[3];
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            try {
                String[] a = s.split(":");
                double b0 = Double.parseDouble(a[0]);
                double b1 = Double.parseDouble(a[1]);
                if (bookInHand) {
                    return b1;
                } else {
                    return b0;
                }
            } catch (Exception e1) {

            }
        }
        return 0.0;
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
        return block.getState(false) instanceof Container;
    }

    private boolean isSign(Block block) {
        Class<?> typeData = block.getType().data;
        return typeData == org.bukkit.block.data.type.Sign.class || typeData == WallSign.class;
    }

    private ChestProtectionState isProtectedChest(Block block, HumanEntity player) {
        BlockState bs = block.getState(false);
        if (bs instanceof Container) {
            Inventory inv = ((Container) bs).getInventory();
            if (inv instanceof DoubleChestInventory) {
                ChestProtectionState rv1 = isThisBlockProtectedChest(((DoubleChestInventory) inv).getLeftSide().getLocation().getBlock(), player);
                if (rv1 == ChestProtectionState.FOREIGN_CHEST) {
                    return ChestProtectionState.FOREIGN_CHEST;
                }
                ChestProtectionState rv2 = isThisBlockProtectedChest(((DoubleChestInventory) inv).getRightSide().getLocation().getBlock(), player);
                if (rv2 == ChestProtectionState.FOREIGN_CHEST) {
                    return ChestProtectionState.FOREIGN_CHEST;
                }
                return (rv1 == ChestProtectionState.OWN_CHEST || rv2 == ChestProtectionState.OWN_CHEST) ? ChestProtectionState.OWN_CHEST : ChestProtectionState.NO_BOOKSHOP_CHEST;

            } else {
                return isThisBlockProtectedChest(inv.getLocation().getBlock(), player);
            }
        }
        return ChestProtectionState.NO_BOOKSHOP_CHEST; // no chest?
    }

    private ChestProtectionState isThisBlockProtectedChest(Block block, HumanEntity player) {
        Block up = block.getRelative(BlockFace.UP);
        if (up != null && isSign(up)) {
            BlockState upState = up.getState();
            if (upState instanceof Sign) {
                Sign sign = (Sign) upState;
                if (plugin.getConfigHandler().isFirstLineOfEveryShop(sign.getLine(0))) {
                    if (!sign.getLine(1).equalsIgnoreCase(plugin.getConfigHandler().getAdminShopName()) && sign.getLine(1).equalsIgnoreCase(plugin.getNameShortener().getShortName(player.getUniqueId(), false))) {
                        return ChestProtectionState.OWN_CHEST;
                    } else if (player.hasPermission("bookshop.admin")) {
                        return ChestProtectionState.OWN_CHEST;
                    } else {
                        return ChestProtectionState.FOREIGN_CHEST;
                    }
                }
            }
        }
        return ChestProtectionState.NO_BOOKSHOP_CHEST;
    }

    private void buyFromShop(Player player, String[] lines, Sign sign) {
        if (!plugin.checkPermission(player, "bookshop.use")) {
            return;
        }
        if (!isPriceLineValid(lines)) {
            plugin.sendErrorMessage(player, plugin.getConfigHandler().getTranslatedString("Shop.error.wrongPrice"));
            return;
        }
        if (plugin.getNameShortener().getShortName(player.getUniqueId(), false).equalsIgnoreCase(lines[1])) {
            plugin.sendErrorMessage(player, "That is your Shop");
            return;
        }
        PlayerInventory playerInventory = player.getInventory();
        if (playerInventory.firstEmpty() == -1) {
            plugin.sendErrorMessage(player, plugin.getConfigHandler().getTranslatedString("Shop.error.inventoryfull"));
            return;
        }

        boolean isAdminShop = lines[1].equalsIgnoreCase(plugin.getConfigHandler().getAdminShopName());
        UUID ownerUUID = isAdminShop ? null : plugin.getNameShortener().getUUID(lines[1]);
        CachedPlayer owner = isAdminShop ? null : (ownerUUID == null ? plugin.getPlayerUUIDCache().getPlayer(lines[1]) : plugin.getPlayerUUIDCache().getPlayer(ownerUUID));

        Inventory chestInventory = null;
        ItemStack item = null;
        BookMeta book = null;
        Container chest = null;
        int existingBooksAmount = 0;
        BlockState blockBelow = sign.getLocation().getBlockY() > 0 ? sign.getBlock().getRelative(BlockFace.DOWN).getState() : null;
        if (blockBelow instanceof Container) {
            chest = (Container) blockBelow;
            chestInventory = chest.getInventory();
            int size = chestInventory.getSize();
            for (int i = 0; i < size; i++) {
                ItemStack stack = chestInventory.getItem(i);
                if (stack != null && stack.getType() == Material.WRITTEN_BOOK) {
                    if (item == null) {
                        book = (BookMeta) stack.getItemMeta();
                        if (isAdminShop || (owner != null && book.getAuthor() != null && book.getAuthor().equalsIgnoreCase(owner.getName()))) {
                            item = stack;
                            existingBooksAmount = stack.getAmount();
                        } else {
                            book = null; // invalid
                        }
                    } else {
                        if (stack.isSimilar(item)) {
                            existingBooksAmount += stack.getAmount();
                        }
                    }
                }
            }
        }
        if (book == null) {
            plugin.sendErrorMessage(player, plugin.getConfigHandler().getTranslatedString("Shop.error.nobook"));
            return;
        }
        item = item.clone();
        item.setAmount(1);

        if (isAdminShop) {
            if (playerInventory.firstEmpty() == -1) {
                plugin.sendErrorMessage(player, "Du hast keinen freien Platz in deinem Inventar!");
                return;
            }
            double price = getPrice(lines, player, false);
            if ((plugin.getEconomyHandler().getBalance(player) - price) < 0 || !plugin.getEconomyHandler().subtractMoney(player, price)) {
                plugin.sendErrorMessage(player, plugin.getConfigHandler().getTranslatedString("Shop.error.notenoughmoneyconsumer"));
                return;
            }
            Bukkit.getPluginManager().callEvent(new TransactionEvent(player, null, book, price));

            playerInventory.addItem(item);
            plugin.sendInfoMessage(player, String.format(plugin.getConfigHandler().getTranslatedString("Shop.success.buy"), book.getTitle(), "AdminShop", plugin.getEconomyHandler().formatMoney(price)));
        } else {
            boolean bookInHand = playerInventory.getItemInMainHand() != null && playerInventory.getItemInMainHand().getType() == Material.WRITABLE_BOOK;
            if (!bookInHand && existingBooksAmount <= 1 && chestInventory.first(Material.WRITABLE_BOOK) < 0) {
                plugin.sendErrorMessage(player, plugin.getConfigHandler().getTranslatedString("Shop.error.takeBookandQuill"));
                return;
            }
            double price = getPrice(lines, player, bookInHand);

            ItemStack toRemove;
            boolean removeFromChest = true;
            if (bookInHand) {
                toRemove = playerInventory.getItemInMainHand().clone();
                removeFromChest = false;
            } else if (existingBooksAmount > 1) {
                toRemove = item.clone();
            } else {
                int slotbook = chestInventory.first(Material.WRITABLE_BOOK);
                toRemove = slotbook >= 0 ? chestInventory.getItem(slotbook).clone() : null;
            }
            toRemove.setAmount(1);

            if (plugin.getEconomyHandler().getBalance(player) < price || !plugin.getEconomyHandler().subtractMoney(player, price)) {
                plugin.sendErrorMessage(player, plugin.getConfigHandler().getTranslatedString("Shop.error.notenoughmoneyconsumer"));
                return;
            }
            if (removeFromChest) {
                chestInventory.removeItem(toRemove);
            } else {
                playerInventory.removeItem(toRemove);
            }
            HashMap<Integer, ItemStack> overflow = playerInventory.addItem(item);
            if (overflow != null && overflow.size() > 0) {
                for (ItemStack o : overflow.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), o);
                }
            }
            plugin.getLogger().info(player.getName() + " (" + player.getUniqueId() + ") hat das Buch '" + book.getTitle() + "' (Author: " + book.getAuthor() + ") von " + (isAdminShop ? "AdminShop" : (owner == null ? lines[1] : (owner.getName() + " (" + owner.getUUID() + ")"))) + " für "
                    + plugin.getEconomyHandler().formatMoney(price) + " gekauft.");

            if (owner != null) {
                plugin.getEconomyHandler().addMoney(plugin.getServer().getOfflinePlayer(owner.getUUID()), price);
            }
            plugin.sendInfoMessage(player, String.format(plugin.getConfigHandler().getTranslatedString("Shop.success.buy"), book.getTitle(), owner != null ? owner.getName() : lines[1], plugin.getEconomyHandler().formatMoney(price)));

            Bukkit.getPluginManager().callEvent(new TransactionEvent(player, owner, book, price));

            Player ownerOnline = owner != null ? plugin.getServer().getPlayer(owner.getUUID()) : null;
            if (ownerOnline != null) {
                plugin.sendInfoMessage(ownerOnline, String.format(plugin.getConfigHandler().getTranslatedString("Shop.success.sellerbuy"), book.getTitle(), player.getName(), plugin.getEconomyHandler().formatMoney(price)));
            }
        }
    }
}
