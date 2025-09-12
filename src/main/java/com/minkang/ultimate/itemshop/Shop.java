package com.minkang.ultimate.itemshop;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Shop {
    private final String name;
    private final int size;
    private ItemStack currency; // 화폐
    private final Map<Integer, ShopItem> items = new HashMap<>();

    public Shop(String name, int size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public ItemStack getCurrency() {
        return currency;
    }

    public void setCurrency(ItemStack currency) {
        this.currency = currency;
    }

    public Map<Integer, ShopItem> getItems() {
        return items;
    }

    public Inventory createInventory() {
        Inventory inv = Bukkit.createInventory(null, size, Main.TITLE_PREFIX + name);
        for (Map.Entry<Integer, ShopItem> e : items.entrySet()) {
            int slot = e.getKey();
            if (slot >= 0 && slot < size) {
                inv.setItem(slot, Util.withPriceLore(e.getValue().getItem().clone(), e.getValue().getPrice()));
            }
        }
        return inv;
    }
}
