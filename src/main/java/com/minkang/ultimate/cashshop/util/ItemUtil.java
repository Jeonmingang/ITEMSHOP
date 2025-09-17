
package com.minkang.ultimate.cashshop.util;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemUtil {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static ItemStack withOverlay(ItemStack base, List<String> overlay) {
        if (base == null) return null;
        ItemStack clone = base.clone();
        ItemMeta meta = clone.getItemMeta();
        List<String> newLore = new ArrayList<>();
        if (meta != null && meta.hasLore()) {
            newLore.addAll(meta.getLore());
        }
        for (String line : overlay) {
            newLore.add(color(line));
        }
        if (meta != null) {
            meta.setLore(newLore);
            clone.setItemMeta(meta);
        }
        return clone;
    }

    public static String fmt(String template, Map<String, String> repl) {
        String out = template;
        for (Map.Entry<String, String> e : repl.entrySet()) {
            out = out.replace("{" + e.getKey() + "}", e.getValue());
        }
        return color(out);
    }
}
