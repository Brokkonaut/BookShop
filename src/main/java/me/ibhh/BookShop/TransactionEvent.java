package me.ibhh.BookShop;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.meta.BookMeta;

public class TransactionEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private OfflinePlayer owner;
    private BookMeta book;
    private double price;

    public TransactionEvent(Player who, OfflinePlayer owner, BookMeta book, double price) {
        super(who);
        this.owner = owner;
        this.book = book;
        this.price = price;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public BookMeta getBook() {
        return book;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
