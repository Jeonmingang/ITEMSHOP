package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static ItemStack withPriceLore(ItemStack stack, int price, ItemStack currency) {
        if (stack == null) return null;
        ItemStack s = stack.clone();
        ItemMeta meta = s.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<String>();

            FileConfiguration c = Main.getInstance().getConfig();
            List<String> lines = c.getStringList("gui.item_lore");
            if (lines == null || lines.isEmpty()) {
                lines = new ArrayList<String>();
                lines.add("&e가격: &f%price%");
                lines.add("&7화폐: &f%currency%");
            }

            String currencyName;
            if (currency != null && currency.getItemMeta() != null && currency.getItemMeta().hasDisplayName()) {
                currencyName = ChatColor.stripColor(currency.getItemMeta().getDisplayName());
            } else if (currency != null) {
                currencyName = currency.getType().name();
            } else {
                currencyName = "N/A";
            }

            for (String line : lines) {
                String t = line.replace("%price%", String.valueOf(price))
                               .replace("%currency%", currencyName);
                lore.add(ChatColor.translateAlternateColorCodes('&', t));
            }
            meta.setLore(lore);
            s.setItemMeta(meta);
        }
        return s;
    }

    public static ItemStack withPriceLore(ItemStack stack, int price) {
        return withPriceLore(stack, price, null);
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
        List<String> al = am.getLore();
        List<String> bl = bm.getLore();
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
