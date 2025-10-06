package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /** 통화명 포함 가격 로어 추가 (권장 시그니처) */
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

    /** 원래 코드와의 호환: (stack, price, currencyName) 시그니처도 지원 */
    public static ItemStack withPriceLore(ItemStack stack, int price, String currencyName) {
        return withPriceLore(stack, currencyName, price);
    }

    /** 플레이어 인벤토리에 동일/유사한 통화 아이템 총량 계산 */
    public static int countCurrency(Player player, ItemStack currencyTemplate) {
        if (player == null || currencyTemplate == null) return 0;
        int count = 0;
        for (ItemStack it : player.getInventory().getContents()) {
            if (it != null && it.isSimilar(currencyTemplate)) {
                count += it.getAmount();
            }
        }
        return count;
    }

    /** 플레이어 인벤토리에 해당 아이템이 들어갈 자리 있는지 여부 (빈칸 or 합쳐 넣기) */
    public static boolean canFit(PlayerInventory inv, ItemStack item) {
        if (inv == null || item == null) return false;
        int maxStack = item.getMaxStackSize();
        // 빈칸
        if (inv.firstEmpty() != -1) return true;
        // 합치기 가능
        for (ItemStack it : inv.getContents()) {
            if (it != null && it.isSimilar(item) && it.getAmount() < Math.min(maxStack, inv.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }

    /** 플레이어 인벤토리에서 통화템을 amount 만큼 제거. 전부 제거 성공 시 true */
    public static boolean removeCurrency(Player player, ItemStack currencyTemplate, int amount) {
        if (player == null || currencyTemplate == null || amount <= 0) return false;
        Inventory inv = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (it == null) continue;
            if (it.isSimilar(currencyTemplate)) {
                int take = Math.min(remaining, it.getAmount());
                int left = it.getAmount() - take;
                if (left <= 0) {
                    inv.setItem(i, null);
                } else {
                    it.setAmount(left);
                    inv.setItem(i, it);
                }
                remaining -= take;
                if (remaining <= 0) break;
            }
        }
        return remaining <= 0;
    }
}
