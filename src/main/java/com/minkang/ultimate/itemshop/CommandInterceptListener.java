package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.util.Vector;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Intercepts: /아이템상점 연동 <상점이름>
 * If npc name is omitted, links the shop to the Citizens NPC the player is looking at (within 6 blocks).
 * No compile-time dependency on Citizens; checks "NPC" metadata on the entity.
 * Compatible with Spigot/Paper 1.16.5: no usage of Player#rayTraceEntities (not available on older APIs).
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

        // Find target entity user is looking at (within 6 blocks), compatible with 1.16
        Entity target = findTargetEntityCompat(e.getPlayer().getLocation(), e.getPlayer(), 6.0);
        if (!(target instanceof LivingEntity) || !target.hasMetadata("NPC")) {
            e.getPlayer().sendMessage(plugin.msg("no_npc_in_sight"));
            e.setCancelled(true);
            return;
        }

        String npcName = target.getName();
        if (npcName == null || npcName.trim().isEmpty()) {
            npcName = ((LivingEntity) target).getCustomName();
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

    /**
     * Try to resolve target entity user is looking at within a distance, without using rayTraceEntities.
     * 1) Try reflection on Player#getTargetEntity(int) if available.
     * 2) Fallback: search nearby entities and choose the one with best alignment and distance along the view ray.
     */
    private Entity findTargetEntityCompat(Location eyeLoc, org.bukkit.entity.Player p, double maxDistance) {
        // 1) Reflection path for newer APIs
        try {
            Method m = p.getClass().getMethod("getTargetEntity", int.class);
            Object result = m.invoke(p, (int) Math.ceil(maxDistance));
            if (result instanceof Entity) return (Entity) result;
        } catch (Throwable ignored) {}

        // 2) Geometric fallback: pick entity in a narrow cone along view direction
        Vector origin = eyeLoc.toVector();
        Vector dir = eyeLoc.getDirection().normalize();
        double bestScore = 0.965; // cos(theta); ~15 degrees cone
        Entity best = null;

        Collection<Entity> nearby = p.getWorld().getNearbyEntities(eyeLoc, maxDistance, maxDistance, maxDistance);
        for (Entity ent : nearby) {
            if (ent == p) continue;
            Vector to = ent.getLocation().toVector().add(new Vector(0, ent.getHeight() * 0.5, 0)).subtract(origin);
            double dist = to.length();
            if (dist < 0.01 || dist > maxDistance) continue;
            Vector norm = to.normalize();
            double dot = dir.dot(norm);
            if (dot > bestScore) {
                bestScore = dot;
                best = ent;
            }
        }
        return best;
    }
}
