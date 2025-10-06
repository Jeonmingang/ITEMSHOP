package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class Util {
    private Util() {}

    /** &컬러코드를 §로 변환 */
    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /** 천 단위 콤마 */
    public static String formatNumber(int n) {
        return String.format("%,d", n);
    }

    /** 통화명 포함 가격 로어 추가 */
    public static ItemStack withPriceLore(ItemStack stack, String currencyName, int price) {
        if (stack == null) return null;
        String cur = (currencyName == null || currencyName.isEmpty()) ? "원" : currencyName;
        String line = color("&7가격: &e" + formatNumber(price) + " &f" + cur);

        ItemStack copy = stack.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            // 기존 가격라인 중복 방지(대략적 필터)
            List<String> newLore = new ArrayList<>();
            for (String l : lore) {
                String plain = ChatColor.stripColor(l);
                if (plain == null || !plain.replace(" ", "").startsWith("가격:")) newLore.add(l);
            }
            newLore.add(line);
            meta.setLore(newLore);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    /** 통화명 기본값 "원" */
    public static ItemStack withPriceLore(ItemStack stack, int price) {
        return withPriceLore(stack, "원", price);
    }
}
