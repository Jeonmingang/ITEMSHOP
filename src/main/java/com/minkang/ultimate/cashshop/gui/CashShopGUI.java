
package com.minkang.ultimate.cashshop.gui;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.store.ShopStore;
import com.minkang.ultimate.cashshop.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CashShopGUI {

    public static String title() {
        FileConfiguration cfg = Main.getInstance().getConfig();
        return ItemUtil.color(cfg.getString("gui.title", "&8후원 상점"));
    }

    public static int size() {
        FileConfiguration cfg = Main.getInstance().getConfig();
        int size = cfg.getInt("gui.size", 54);
        if (size % 9 != 0) size = 54;
        if (size < 9) size = 9;
        if (size > 54) size = 54;
        return size;
    }

    public static void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, size(), title());
        List<String> overlayRaw = Main.getInstance().getConfig().getStringList("lore.overlay");

        for (Map.Entry<Integer, ShopStore.Listing> e : Main.getInstance().getShopStore().all().entrySet()) {
            int slot = e.getKey();
            ShopStore.Listing l = e.getValue();
            if (slot < 0 || slot >= inv.getSize() || l.item == null) continue;
            Map<String, String> repl = new HashMap<>();
            repl.put("price", Long.toString(l.price));
            repl.put("amount", Integer.toString(l.amount));

            List<String> overlay = new ArrayList<>();
            for (String line : overlayRaw) {
                overlay.add(ItemUtil.fmt(line, repl));
            }
            ItemStack display = ItemUtil.withOverlay(l.item, overlay);
            inv.setItem(slot, display);
        }
        p.openInventory(inv);
    }
}
