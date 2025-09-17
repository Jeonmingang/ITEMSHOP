
package com.minkang.ultimate.cashshop.util;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.gui.CashShopGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Method;

public class CitizenHook {

    public static void tryRegister(Main plugin) {
        try {
            // Try to bind NPCRightClickEvent dynamically
            Class<?> npcRightClick = Class.forName("net.citizensnpcs.api.event.NPCRightClickEvent");
            PluginManager pm = Bukkit.getPluginManager();
            pm.registerEvent((Class<? extends Event>) npcRightClick, new Listener(){},
                    EventPriority.NORMAL, new EventExecutor() {
                        @Override
                        public void execute(Listener listener, Event event) {
                            try {
                                Method getNPC = event.getClass().getMethod("getNPC");
                                Object npc = getNPC.invoke(event);
                                Method getId = npc.getClass().getMethod("getId");
                                int id = (int) getId.invoke(npc);

                                if (!Main.getInstance().getLinkStore().isLinked(id)) return;

                                Method getClicker = event.getClass().getMethod("getClicker");
                                Object clicker = getClicker.invoke(event);
                                if (clicker instanceof Player) {
                                    CashShopGUI.open((Player) clicker);
                                }
                            } catch (Throwable ignored) {}
                        }
                    }, plugin, true);
            plugin.getLogger().info("[Citizens] NPCRightClickEvent hooked successfully.");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("[Citizens] Not found. NPC linking disabled until Citizens is installed.");
        } catch (Throwable t) {
            plugin.getLogger().warning("[Citizens] Hook failed: " + t.getMessage());
        }
    }
}
