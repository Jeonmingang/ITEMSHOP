package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    private final Main plugin;

    public GuiListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        String title = e.getView().getTitle();
        if (title == null || !title.startsWith(Main.TITLE_PREFIX)) return;
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        Inventory top = e.getView().getTopInventory();
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.CHEST) return;
        if (!e.getClickedInventory().equals(top)) return;

        String shopName = title.substring(Main.TITLE_PREFIX.length());
        Shop shop = plugin.getShopManager().get(shopName);
        if (shop == null) {
            p.closeInventory();
            p.sendMessage(plugin.msg("shop_missing"));
            return;
        }

        int slot = e.getSlot();
        ShopItem si = shop.getItems().get(slot);
        if (si == null) return;

        ItemStack currency = shop.getCurrency();
        if (currency == null) {
            p.sendMessage(plugin.msg("no_currency_set"));
            return;
        }

        int price = si.getPrice();
        int have = Util.countCurrency(p, currency);

        if (have < price) {
            p.sendMessage(plugin.msg("no_currency").replace("%have%", String.valueOf(have)).replace("%price%", String.valueOf(price)));
            return;
        }

        ItemStack toGive = si.getItem().clone();
        if (p.getInventory().firstEmpty() == -1 && !p.getInventory().addItem(toGive).isEmpty()) {
            p.sendMessage(plugin.msg("no_space"));
            return;
        } else {
            p.getInventory().removeItem(toGive);
        }

        boolean removed = Util.removeCurrency(p, currency, price);
        if (!removed) {
            p.sendMessage(plugin.msg("removed_fail"));
            return;
        }

        p.getInventory().addItem(toGive);
        p.updateInventory();
        plugin.playBuyEffect(p);
        String itemName = toGive.getType().name();
        if (toGive.getItemMeta() != null && toGive.getItemMeta().hasDisplayName()) {
            itemName = ChatColor.stripColor(toGive.getItemMeta().getDisplayName());
        }
        p.sendMessage(plugin.msg("bought").replace("%item%", itemName).replace("%amount%", String.valueOf(toGive.getAmount())).replace("%price%", String.valueOf(price)));
    }

    @EventHandler(ignoreCancelled = true)
    public void onNpcRightClick(PlayerInteractEntityEvent e) {
        handleNpcClick(e.getPlayer(), e.getRightClicked());
    }

    @EventHandler(ignoreCancelled = true)
    public void onNpcRightClick2(PlayerInteractAtEntityEvent e) {
        handleNpcClick(e.getPlayer(), e.getRightClicked());
    }

    private void handleNpcClick(Player p, Entity entity) {
        if (entity == null) return;
        String name = entity.getCustomName();
        if (name == null) return;
        String linked = plugin.getShopManager().getLinkedShopName(name);
        if (linked == null) return;
        Shop shop = plugin.getShopManager().get(linked);
        if (shop == null) return;
        p.openInventory(shop.createInventory());
        p.sendMessage(plugin.msg("open_by_npc").replace("%shop%", shop.getName()));
    }
}
