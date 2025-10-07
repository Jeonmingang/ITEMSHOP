package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Adds two patterns (ID 기반):
 *  - /아이템상점 연동 <npcId> <상점이름>  : 해당 NPC ID에 상점 연동
 *  - /아이템상점 연동 <npcId>         : 해당 NPC ID 연동 해제
 * 기존 이름 기반 명령어는 ShopCommand 쪽에 그대로 남겨둠(호환).
 */
public class CommandInterceptListener implements Listener {

    private final Main plugin;
    public CommandInterceptListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (msg == null || !msg.startsWith("/")) return;
        String[] parts = msg.substring(1).trim().split("\\s+");
        if (parts.length < 2) return;
        String label = parts[0];
        if (!(label.equalsIgnoreCase("아이템상점") || label.equalsIgnoreCase("itemshop"))) return;
        if (!(parts[1].equalsIgnoreCase("연동") || parts[1].equalsIgnoreCase("link"))) return;

        // /아이템상점 연동 <npcId> <shopName>
        if (parts.length == 4) {
            Integer npcId = parseInt(parts[2]);
            if (npcId == null) return; // not our form
            String shopName = parts[3];

            if (!plugin.getShopManager().exists(shopName)) {
                e.getPlayer().sendMessage(plugin.msg("shop_missing"));
                e.setCancelled(true);
                return;
            }
            plugin.getShopManager().linkNpcId(npcId, shopName);
            plugin.getShopManager().save();

            String tpl = plugin.getConfig().getString("messages.linked_id", "&aNPC &b#{id}&a 에 상점 &e{shop}&a 연동 완료.");
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    tpl.replace("{id}", String.valueOf(npcId)).replace("{shop}", shopName)));
            e.setCancelled(true);
            return;
        }

        // /아이템상점 연동 <npcId>  => unlink
        if (parts.length == 3) {
            Integer npcId = parseInt(parts[2]);
            if (npcId == null) return; // not our form
            plugin.getShopManager().unlinkNpcId(npcId);
            plugin.getShopManager().save();
            String tpl = plugin.getConfig().getString("messages.unlinked_id", "&eNPC &b#{id}&e 연동을 해제했습니다.");
            e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    tpl.replace("{id}", String.valueOf(npcId))));
            e.setCancelled(true);
        }
    }

    private Integer parseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException ex) { return null; }
    }
}
