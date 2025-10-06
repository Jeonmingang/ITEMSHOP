package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.util.RayTraceResult;

/**
 * Intercepts: /아이템상점 연동 <상점이름>
 * If npc name is omitted, links the shop to the Citizens NPC the player is looking at (within 6 blocks).
 * No compile-time dependency on Citizens; checks "NPC" metadata on the entity.
 */
public class CommandInterceptListener implements Listener {

    private final Main plugin;

    public CommandInterceptListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (msg == null || !msg.startsWith("/")) return;
        String[] parts = msg.substring(1).trim().split("\\s+");
        if (parts.length != 3) return; // we only care about exactly 3 tokens: cmd sub shop
        String label = parts[0];
        if (!(label.equalsIgnoreCase("아이템상점") || label.equalsIgnoreCase("itemshop"))) return;
        String sub = parts[1];
        if (!(sub.equalsIgnoreCase("연동") || sub.equalsIgnoreCase("link"))) return;

        String shopName = parts[2];

        // Try to find a Citizens NPC by raytrace
        RayTraceResult res = e.getPlayer().rayTraceEntities(6.0);
        if (res == null || res.getHitEntity() == null || !(res.getHitEntity() instanceof LivingEntity)) {
            e.getPlayer().sendMessage(plugin.msg("no_npc_in_sight"));
            e.setCancelled(true);
            return;
        }
        Entity ent = res.getHitEntity();
        if (!ent.hasMetadata("NPC")) { // Citizens adds "NPC" metadata
            e.getPlayer().sendMessage(plugin.msg("no_npc_in_sight"));
            e.setCancelled(true);
            return;
        }

        String npcName = ent.getName();
        if (npcName == null || npcName.trim().isEmpty()) {
            npcName = ((LivingEntity) ent).getCustomName();
            if (npcName == null || npcName.trim().isEmpty()) npcName = "NPC";
        }

        // Link and persist
        plugin.getShopManager().linkNpc(npcName, shopName);
        plugin.getShopManager().save();

        String msgTemplate = plugin.getConfig().getString("messages.linked", "&a상점 &e{shop}&a 이(가) NPC &b{npc}&a 에 연동되었습니다.");
        e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                msgTemplate.replace("{shop}", shopName).replace("{npc}", npcName)));

        // Stop the original executor from handling (which expects npc name and would error)
        e.setCancelled(true);
    }
}
