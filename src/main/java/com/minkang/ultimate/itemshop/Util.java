package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
            // Configurable: include original item lore in GUI
            boolean showOriginal = c.getBoolean("gui.show_original_lore", true);
            int maxOrigLines = Math.max(0, c.getInt("gui.original_lore_max_lines", 12));
            String origPrefix = c.getString("gui.original_lore_prefix", "&7");
            String separator = c.getString("gui.lore_separator", "&8----------------");

            List<String> out = new ArrayList<String>();
            if (showOriginal && !lore.isEmpty()) {
                int count = 0;
                for (String line : lore) {
                    if (count >= maxOrigLines) break;
                    String colored = ChatColor.translateAlternateColorCodes('&', (origPrefix == null ? "" : origPrefix) + line);
                    out.add(colored);
                    count++;
                }
                // Add separator between original lore and price block
                if (separator != null && !separator.isEmpty()) {
                    out.add(ChatColor.translateAlternateColorCodes('&', separator));
                }
            }

            // Price block template
            List<String> templ = c.getStringList("gui.item_lore");
            if (templ == null || templ.isEmpty()) {
                templ = new ArrayList<String>();
                templ.add("&e가격: &f%price%");
                templ.add("&7화폐: &f%currency%");
            }

            String currencyName;
            if (currency != null && currency.getItemMeta() != null && currency.getItemMeta().hasDisplayName()) {
                currencyName = ChatColor.stripColor(currency.getItemMeta().getDisplayName());
            } else if (currency != null) {
                currencyName = currency.getType().name();
            } else {
                currencyName = "N/A";
            }

            for (String line : templ) {
                String t = line.replace("%price%", String.valueOf(price)).replace("%currency%", currencyName);
                out.add(ChatColor.translateAlternateColorCodes('&', t));
            }
            meta.setLore(out);
            s.setItemMeta(meta);
        }
        return s;
    }
            String currencyName;
            if (currency != null && currency.getItemMeta() != null && currency.getItemMeta().hasDisplayName()) {
                currencyName = ChatColor.stripColor(currency.getItemMeta().getDisplayName());
            } else if (currency != null) {
                currencyName = currency.getType().name();
            } else {
                currencyName = "N/A";
            }
            for (String line : templ) {
                String t = line.replace("%price%", String.valueOf(price)).replace("%currency%", currencyName);
                lore.add(ChatColor.translateAlternateColorCodes('&', t));
            }
            meta.setLore(lore);
            s.setItemMeta(meta);
        }
        return s;
    }

    public static ItemStack withPriceLore(ItemStack stack, int price) { return withPriceLore(stack, price, null); }

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

    /** 인벤토리에 item 전체 수량이 들어갈 수 있는지 확인 (빈칸 + 같은 스택 병합 용량 계산) */
    public static boolean canFit(Inventory inv, ItemStack item) {
        if (item == null) return false;
        int need = item.getAmount();
        int max = Math.max(1, item.getMaxStackSize());
        int capacity = 0;

        ItemStack[] cont = inv.getContents();
        for (ItemStack is : cont) {
            if (is == null || is.getType() == Material.AIR) {
                capacity += max; // 빈칸은 새 스택 하나
            } else if (isSimilarIgnoreAmount(is, item)) {
                capacity += (max - is.getAmount()); // 같은 스택에 병합
            }
            if (capacity >= need) return true;
        }
        return capacity >= need;
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
                if (need <= 0) { p.updateInventory(); return true; }
            }
        }
        p.updateInventory();
        return false;
    }

    public static String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
