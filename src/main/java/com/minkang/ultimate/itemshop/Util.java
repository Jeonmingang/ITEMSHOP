\
    package com.minkang.ultimate.itemshop;

    import org.bukkit.ChatColor;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.inventory.meta.ItemMeta;

    import java.util.ArrayList;
    import java.util.List;

    /**
     * Minimal, compile-safe Utils for ItemShop.
     * Replace your current Util.java with this if you saw "illegal start of expression/type"
     * errors due to duplicated stray blocks.
     */
    public final class Util {

        private Util() {}

        /** Translate '&' color codes to §. */
        public static String color(String s) {
            if (s == null) return "";
            return ChatColor.translateAlternateColorCodes('&', s);
        }

        /** Simple number format with thousand separators. */
        public static String formatNumber(int n) {
            return String.format("%,d", n);
        }

        /**
         * Return a clone of the stack with price lore applied.
         * Example lore: ["§7가격: §e1,000 §f원"]
         */
        public static ItemStack withPriceLore(ItemStack stack, String currencyName, int price) {
            if (stack == null) return null;
            String cur = (currencyName == null || currencyName.isEmpty()) ? "원" : currencyName;
            String line = color("&7가격: &e" + formatNumber(price) + " &f" + cur);

            ItemStack copy = stack.clone();
            ItemMeta meta = copy.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                // remove existing price line if present (basic heuristic)
                lore.removeIf(l -> ChatColor.stripColor(l).startsWith("가격:"));
                lore.add(line);
                meta.setLore(lore);
                copy.setItemMeta(meta);
            }
            return copy;
        }

        /**
         * Overload: use default currency name ("원").
         */
        public static ItemStack withPriceLore(ItemStack stack, int price) {
            return withPriceLore(stack, "원", price);
        }
    }
