
package com.minkang.ultimate.cashshop.listeners;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.gui.CashShopGUI;
import com.minkang.ultimate.cashshop.store.ShopStore;
import com.minkang.ultimate.cashshop.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        Inventory top = e.getView().getTopInventory();
        if (top == null) return;
        String title = e.getView().getTitle();
        if (!title.equals(CashShopGUI.title())) return;

        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot < 0 || slot >= top.getSize()) return;

        Optional<ShopStore.Listing> opt = Main.getInstance().getShopStore().get(slot);
        if (!opt.isPresent()) {
            String msg = Main.getInstance().getConfig().getString("messages.not_listed");
            if (msg != null) p.sendMessage(ItemUtil.color(msg));
            return;
        }
        ShopStore.Listing l = opt.get();

        long have = Main.getInstance().getBalanceStore().get(p.getUniqueId());
        if (have < l.price) {
            Map<String, String> repl = new HashMap<>();
            repl.put("price", Long.toString(l.price));
            repl.put("balance", Long.toString(have));
            String msg = Main.getInstance().getConfig().getString("messages.need_cash");
            if (msg != null) p.sendMessage(ItemUtil.fmt(msg, repl));
            return;
        }

        ItemStack toGive = l.item.clone();
        toGive.setAmount(l.amount);

        Map<Integer, ItemStack> left = p.getInventory().addItem(toGive);
        if (!left.isEmpty()) {
            String msg = Main.getInstance().getConfig().getString("messages.need_space");
            if (msg != null) p.sendMessage(ItemUtil.color(msg));
            return;
        }

        // deduct
        Main.getInstance().getBalanceStore().add(p.getUniqueId(), -l.price);

        long now = Main.getInstance().getBalanceStore().get(p.getUniqueId());
        Map<String, String> repl = new HashMap<>();
        repl.put("amount", Integer.toString(l.amount));
        repl.put("price", Long.toString(l.price));
        repl.put("balance", Long.toString(now));
        String msg = Main.getInstance().getConfig().getString("messages.bought");
        if (msg != null) p.sendMessage(ItemUtil.fmt(msg, repl));
    }
}
