package com.minkang.ultimate.itemshop;

import org.bukkit.inventory.ItemStack;

public class ShopItem {
    private final ItemStack item;
    private final int price;
    public ShopItem(ItemStack item, int price) { this.item = item; this.price = price; }
    public ItemStack getItem() { return item; }
    public int getPrice() { return price; }
}
