package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Util {

    public static ItemStack withPriceLore(ItemStack stack, int price) {
        if (stack == null) return null;
        ItemStack s = stack.clone();
        ItemMeta meta = s.getItemMeta();
        if (meta != null) {
            java.util.List<String> lore = meta.getLore();
            if (lore == null) lore = new java.util.ArrayList<String>();
            lore.add(ChatColor.YELLOW + "가격: " + price);
            meta.setLore(lore);
            s.setItemMeta(meta);
        }
        return s;
    }

    public static boolean isSimilarIgnoreAmount(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        if (a.getType() != b.getType()) return false;
        ItemMeta am = a.getItemMeta();
        ItemMeta bm = b.getItemMeta();
        if (am == null && bm == null) return true;
        if (am == null || bm == null) return false;
        if (am.hasDisplayName() != bm.hasDisplayName()) return false;
        if (am.hasDisplayName() && !am.getDisplayName().equals(bm.getDisplayName())) return false;
        java.util.List<String> al = am.getLore();
        java.util.List<String> bl = bm.getLore();
        if (al == null && bl == null) return true;
        if (al == null || bl == null) return false;
        return al.equals(bl);
    }

    public static int countCurrency(Player p, ItemStack currency) {
        int count = 0;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is == null || is.getType() == Material.AIR) continue;
            if (isSimilarIgnoreAmount(is, currency)) count += is.getAmount();
        }
        return count;
    }

    public static boolean removeCurrency(Player p, ItemStack currency, int amount) {
        int need = amount;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is == null) continue;
            if (isSimilarIgnoreAmount(is, currency)) {
                int take = Math.min(need, is.getAmount());
                is.setAmount(is.getAmount() - take);
                p.getInventory().setItem(i, is.getAmount() <= 0 ? null : is);
                need -= take;
                if (need <= 0) {
                    p.updateInventory();
                    return true;
                }
            }
        }
        p.updateInventory();
        return false;
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
