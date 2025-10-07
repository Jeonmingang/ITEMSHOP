package com.minkang.ultimate.itemshop;

import com.minkang.ultimate.itemshop.Shop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GuiListener implements Listener {
    private Integer getCitizensId(org.bukkit.entity.Entity ent) {
        try {
            if (!ent.hasMetadata("NPC")) return null;
            java.util.List<org.bukkit.metadata.MetadataValue> vals = ent.getMetadata("NPC");
            if (vals == null || vals.isEmpty()) return null;
            Object npcObj = vals.get(0).value();
            if (npcObj == null) return null;
            // reflection call: npcObj.getClass().getMethod("getId").invoke(npcObj)
            java.lang.reflect.Method m = npcObj.getClass().getMethod("getId");
            Object id = m.invoke(npcObj);
            if (id instanceof Integer) return (Integer) id;
            if (id != null) return Integer.parseInt(String.valueOf(id));
        } catch (Throwable ignored) {}
        return null;
    }


    private final Main plugin;
    private final Map<UUID, Long> openCooldown = new ConcurrentHashMap<UUID, Long>();
    private static final long OPEN_COOLDOWN_MS = 250L;

    public GuiListener(Main plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
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

        // 공간 체크만 수행 (인벤 변경 X)
        if (!Util.canFit(p.getInventory(), toGive)) {
            p.sendMessage(plugin.msg("no_space"));
            return;
        }

        // 먼저 화폐 제거
        boolean removed = Util.removeCurrency(p, currency, price);
        if (!removed) {
            p.sendMessage(plugin.msg("removed_fail"));
            return;
        }

        // 아이템 지급
        p.getInventory().addItem(toGive);
        p.updateInventory();
        plugin.playBuyEffect(p);
        String itemName = toGive.getType().name();
        if (toGive.getItemMeta() != null && toGive.getItemMeta().hasDisplayName()) {
            itemName = ChatColor.stripColor(toGive.getItemMeta().getDisplayName());
        }
        p.sendMessage(plugin.msg("bought").replace("%item%", itemName).replace("%amount%", String.valueOf(toGive.getAmount())).replace("%price%", String.valueOf(price)));
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onNpcRightClick(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        handleNpcClick(e.getPlayer(), e.getRightClicked());
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onNpcRightClick2(PlayerInteractAtEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        handleNpcClick(e.getPlayer(), e.getRightClicked());
    }

    
private void handleNpcClick(Player p, Entity entity) {
        if (entity == null) return;

        long now = System.currentTimeMillis();
        Long last = openCooldown.get(p.getUniqueId());
        if (last != null && (now - last) < OPEN_COOLDOWN_MS) return;
        openCooldown.put(p.getUniqueId(), now);

        // 1) Try Citizens NPC ID first (works even when the NPC has no custom nameplate)
        Integer npcId = getCitizensId(entity);
        String linked = (npcId != null ? plugin.getShopManager().getLinkedShopNameById(npcId) : null);

        // 2) Fallback to name-based linking only if id-link not found
        if (linked == null) {
            String name = entity.getCustomName();
            if (name != null) {
                linked = plugin.getShopManager().getLinkedShopName(name);
            }
        }

        if (linked == null) return;
        Shop shop = plugin.getShopManager().get(linked);
        if (shop == null) return;

        p.openInventory(shop.createInventory());
        p.sendMessage(plugin.msg("open_by_npc").replace("%shop%", shop.getName()));
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onItemRightClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return;
        ItemMeta meta = hand.getItemMeta();
        if (meta == null) return;
        NamespacedKey key = new NamespacedKey(plugin, "shop_opener");
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(key, PersistentDataType.STRING)) return;
        String shopName = pdc.get(key, PersistentDataType.STRING);
        if (shopName == null || shopName.isEmpty()) return;
        Shop shop = plugin.getShopManager().get(shopName);
        if (shop == null) { p.sendMessage(plugin.msg("shop_missing")); return; }

        long now = System.currentTimeMillis();
        Long last = openCooldown.get(p.getUniqueId());
        if (last != null && (now - last) < OPEN_COOLDOWN_MS) return;
        openCooldown.put(p.getUniqueId(), now);

        e.setCancelled(true); // open without any permission checks
        p.openInventory(shop.createInventory());
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage().trim();
        if (!msg.startsWith("/")) return;
        String[] parts = msg.substring(1).split("\\s+");
        if (parts.length < 3) return;
        String base = parts[0];
        String sub = parts[1];
        if (!(base.equals("아이템상점") || base.equalsIgnoreCase("itemshop"))) return;
        if (!sub.equals("아이템")) return;

        Player p = e.getPlayer();
        // admin-only: setting the opener
        if (!p.hasPermission("ultimate.itemshop.admin")) {
            p.sendMessage("§c권한이 없습니다.");
            e.setCancelled(true);
            return;
        }

        String shopName = parts[2];
        Shop shop = plugin.getShopManager().get(shopName);
        if (shop == null) {
            p.sendMessage(plugin.msg("shop_missing"));
            e.setCancelled(true);
            return;
        }

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            p.sendMessage("§c손에 등록할 아이템을 들고 사용해주세요.");
            e.setCancelled(true);
            return;
        }
        ItemMeta meta = hand.getItemMeta();
        if (meta == null) {
            p.sendMessage("§c이 아이템에는 메타데이터를 저장할 수 없습니다.");
            e.setCancelled(true);
            return;
        }
        NamespacedKey key = new NamespacedKey(plugin, "shop_opener");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, shopName);
        hand.setItemMeta(meta);
        p.sendMessage("§a상점 오픈 아이템 설정 완료: §f" + shopName + " §7(이제 이 아이템을 우클릭하면 상점이 열립니다)");
        e.setCancelled(true);
    }


    // === NPC 무권한 오픈 (ID 연동 기반) ===
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onNpcRightClickBypass(PlayerInteractEntityEvent e) {
        // 오프핸드 중복 호출 방지
        try {
            if (e.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) return;
        } catch (Throwable ignored) {}
        handleNpcBypassOpen(e.getPlayer(), e.getRightClicked());
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onNpcRightClickAtBypass(PlayerInteractAtEntityEvent e) {
        try {
            if (e.getHand() == org.bukkit.inventory.EquipmentSlot.OFF_HAND) return;
        } catch (Throwable ignored) {}
        handleNpcBypassOpen(e.getPlayer(), e.getRightClicked());
        e.setCancelled(true);
    }

    private void handleNpcBypassOpen(Player p, Entity clicked) {
        Integer npcId = getCitizensNpcId(clicked);
        if (npcId == null) return;

        Main plugin = Main.getInstance();
        String shopName = plugin.getShopManager().getLinkedShopNameById(npcId);
        boolean hasLink = shopName != null && !shopName.isEmpty();
        boolean bypassCfg = plugin.getConfig().getBoolean("bypass-permission-on-npc", true);
        boolean bypass = bypassCfg && hasLink;

        if (!hasLink) return;
        if (!p.hasPermission("ultimate.itemshop.use") && !bypass) {
            // NPC 링크가 없거나 우회 비활성화면 그냥 무시
            return;
        }

        Shop shop = plugin.getShopManager().get(shopName);
        if (shop == null) {
            p.sendMessage(plugin.msg("shop_missing"));
            return;
        }

        // 오픈 & 피드백
        shop.open(p);
        // 선택적으로 메시지
        try {
            String msg = plugin.getConfig().getString("messages.open_by_npc", "&7상점 &f%shop% &7이(가) 열렸습니다.");
            if (msg != null && !msg.isEmpty()) {
                p.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', msg.replace("%shop%", shopName)));
            }
        } catch (Throwable ignored) {}
    }

    // Citizens API가 없더라도 리플렉션/메타데이터로 NPC ID를 안전하게 구함
    private Integer getCitizensNpcId(Entity entity) {
        if (entity == null) return null;
        // 1) 정식 리플렉션 경로
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
        } catch (Throwable ignored) {}

        // 2) 메타데이터 기반(보조)
        try {
            if (entity.hasMetadata("NPC")) {
                java.util.List<org.bukkit.metadata.MetadataValue> vals = entity.getMetadata("NPC");
                if (vals != null && !vals.isEmpty()) {
                    Object v = vals.get(0).value();
                    if (v != null) {
                        try {
                            java.lang.reflect.Method m = v.getClass().getMethod("getId");
                            Object id = m.invoke(v);
                            if (id instanceof Integer) return (Integer) id;
                            if (id != null) return Integer.parseInt(String.valueOf(id));
                        } catch (Throwable ignored2) {
                            // 마지막 시도: 문자열에서 숫자 추출
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
