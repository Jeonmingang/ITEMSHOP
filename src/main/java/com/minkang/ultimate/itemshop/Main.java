package com.minkang.ultimate.itemshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private ShopManager shopManager;

    public static String TITLE_PREFIX = "§a아이템상점: "; // GUI 제목 접두사 (config에서 로드)

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadFromDisk();

        // 명령어 등록
        if (getCommand("아이템상점") != null) {
            ShopCommand cmd = new ShopCommand(this);
            getCommand("아이템상점").setExecutor(cmd);
            getCommand("아이템상점").setTabCompleter(cmd);
        }

        // 리스너 등록
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("[UltimateItemShop] 활성화 완료");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.save();
        }
        getLogger().info("[UltimateItemShop] 비활성화 완료");
    }

    public void reloadFromDisk() {
        reloadConfig();
        FileConfiguration c = getConfig();
        TITLE_PREFIX = ChatColor.translateAlternateColorCodes('&', c.getString("title_prefix", "&a아이템상점: "));
        if (shopManager == null) {
            shopManager = new ShopManager(this);
        }
        shopManager.load();
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public String msg(String path) {
        String prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix", ""));
        String body = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages."+path, path));
        return prefix + body;
    }

    public void playBuyEffect(org.bukkit.entity.Player p) {
        if (getConfig().getBoolean("effects.play_sound", true)) {
            String s = getConfig().getString("effects.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
            try {
                p.playSound(p.getLocation(), Sound.valueOf(s), 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
