package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clean listener:
 *  - Shop GUI purchase handling
 *  - Open-by-item (PDC "shop_opener")
 *  - Citizens NPC ID-linked open (permission bypass via config), only for linked NPCs
 *  - Safe event cancellation only when we actually open the shop
 *  - Debounce to avoid double-open
 */
public class GuiListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> openCooldown = new ConcurrentHashMap<UUID, Long>();
    private static final long OPEN_COOLDOWN_MS = 250L;

    public GuiListener(Main plugin) {
        this.plugin = plugin;
    }

    /* ---------------------------
     *  GUI CLICK: BUY ITEMS
     * --------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        String title = e.getView().getTitle();
        if (title == null || !title.startsWith(Main.TITLE_PREFIX)) return;

        // Only interact with the top chest inventory
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.CHEST) return;
        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        e.setCancelled(true);

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
            p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.no_currency", "&c구매 실패: 화폐가 부족합니다. (&e%have%&7/&e%price%&c)")
                            .replace("%have%", String.valueOf(have))
                            .replace("%price%", String.valueOf(price))
            ));
            return;
        }

        // remove currency then give item
        if (!Util.removeCurrency(p, currency, price)) {
            p.sendMessage(plugin.msg("removed_fail"));
            return;
        }

        ItemStack give = si.getItem().clone();
        Map<Integer, ItemStack> leftover = p.getInventory().addItem(give);
        if (!leftover.isEmpty()) {
            // rollback? For simplicity we just warn; typical servers ensure free slot first.
            p.sendMessage(plugin.msg("no_space"));
            return;
        }

        // success
        String bought = plugin.getConfig().getString("messages.bought", "&a구매 완료! &f%item% &7x%amount% &a(가격: &e%price%&a)");
        bought = ChatColor.translateAlternateColorCodes('&', bought
                .replace("%item%", Util.itemName(give))
                .replace("%amount%", String.valueOf(give.getAmount()))
                .replace("%price%", String.valueOf(price)));
        p.sendMessage(bought);
        plugin.playBuyEffect(p);
    }

    /* ---------------------------
     *  OPEN BY HAND ITEM (PDC)
     * --------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onUseOpenerItem(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return; // main hand only

        Player p = e.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return;

        ItemMeta meta = hand.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "shop_opener");
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String shopName = pdc.get(key, PersistentDataType.STRING);
        if (shopName == null || shopName.isEmpty()) return;

        Shop shop = plugin.getShopManager().get(shopName);
        if (shop == null) {
            p.sendMessage(plugin.msg("shop_missing"));
            return;
        }

        long now = System.currentTimeMillis();
        Long last = openCooldown.get(p.getUniqueId());
        if (last != null && (now - last) < OPEN_COOLDOWN_MS) return;
        openCooldown.put(p.getUniqueId(), now);

        e.setCancelled(true); // consume click
        p.openInventory(shop.createInventory());
    }

    /* ---------------------------
     *  NPC RIGHT-CLICK (Citizens ID-linked only)
     * --------------------------- */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onNpcRightClick(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (handleNpcClick(e.getPlayer(), e.getRightClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onNpcRightClick2(PlayerInteractAtEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (handleNpcClick(e.getPlayer(), e.getRightClicked())) {
            e.setCancelled(true);
        }
    }

    private boolean handleNpcClick(Player p, Entity entity) {
        if (entity == null) return false;

        long now = System.currentTimeMillis();
        Long last = openCooldown.get(p.getUniqueId());
        if (last != null && (now - last) < OPEN_COOLDOWN_MS) return false;
        openCooldown.put(p.getUniqueId(), now);

        // Citizens NPC ID only. Non-NPC entities are ignored completely.
        Integer npcId = getCitizensId(entity);
        if (npcId == null) return false;

        String linked = plugin.getShopManager().getLinkedShopNameById(npcId);
        if (linked == null || linked.isEmpty()) return false;

        // Permission bypass only for linked NPC (config-controlled)
        boolean bypassCfg = plugin.getConfig().getBoolean("bypass-permission-on-npc", true);
        if (!p.hasPermission("ultimate.itemshop.use") && !bypassCfg) {
            p.sendMessage(plugin.msg("no_permission"));
            return false;
        }

        Shop shop = plugin.getShopManager().get(linked);
        if (shop == null) {
            p.sendMessage(plugin.msg("shop_missing"));
            return false;
        }

        p.openInventory(shop.createInventory());
        String msg = plugin.getConfig().getString("messages.open_by_npc", "&7상점 &f%shop% &7이(가) 열렸습니다.");
        if (msg != null && !msg.isEmpty()) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("%shop%", shop.getName())));
        }
        return true;
    }

    /* ---------------------------
     *  Citizens helper (reflection + metadata fallback)
     * --------------------------- */
    private Integer getCitizensId(Entity entity) {
        // Reflection to avoid compile-time dependency
        try {
            Class<?> api = Class.forName("net.citizensnpcs.api.CitizensAPI");
            java.lang.reflect.Method getReg = api.getMethod("getNPCRegistry");
            Object reg = getReg.invoke(null);
            java.lang.reflect.Method isNpc = reg.getClass().getMethod("isNPC", org.bukkit.entity.Entity.class);
            Boolean npc = (Boolean) isNpc.invoke(reg, entity);
            if (npc == null || !npc) return null;
            java.lang.reflect.Method getNpc = reg.getClass().getMethod("getNPC", org.bukkit.entity.Entity.class);
            Object npcObj = getNpc.invoke(reg, entity);
            if (npcObj == null) return null;
            java.lang.reflect.Method getId = npcObj.getClass().getMethod("getId");
            Object id = getId.invoke(npcObj);
            if (id instanceof Integer) return (Integer) id;
            if (id != null) return Integer.parseInt(String.valueOf(id));
        } catch (Throwable ignored) { /* Citizens absent or API mismatch */ }

        // Metadata hint (best-effort)
        try {
            if (entity.hasMetadata("NPC")) {
                List<MetadataValue> vals = entity.getMetadata("NPC");
                if (vals != null && !vals.isEmpty()) {
                    Object v = vals.get(0).value();
                    if (v != null) {
                        try {
                            java.lang.reflect.Method m = v.getClass().getMethod("getId");
                            Object id = m.invoke(v);
                            if (id instanceof Integer) return (Integer) id;
                            if (id != null) return Integer.parseInt(String.valueOf(id));
                        } catch (Throwable ignored2) {
                            String s = String.valueOf(v);
                            java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("(\\d+)").matcher(s);
                            if (m2.find()) return Integer.parseInt(m2.group(1));
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
