package com.minkang.ultimate.itemshop;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShopManager {

    private final Main plugin;
    private final File file;
    private FileConfiguration data;

    private final Map<String, Shop> shops = new HashMap<String, Shop>();
    private final Map<String, String> npcLinks = new HashMap<String, String>();

    public ShopManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shops.yml");
    }

    public void load() {
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()) { try { file.createNewFile(); } catch (IOException ignored) {} }
        this.data = YamlConfiguration.loadConfiguration(file);
        shops.clear(); npcLinks.clear();

        ConfigurationSection shopsSec = data.getConfigurationSection("shops");
        if (shopsSec != null) {
            for (String name : shopsSec.getKeys(false)) {
                ConfigurationSection s = shopsSec.getConfigurationSection(name);
                if (s == null) continue;
                ItemStack currency = s.getItemStack("currency");
                int size = s.getInt("size", 54);
                Shop shop = new Shop(name, size);
                shop.setCurrency(currency);
                ConfigurationSection itemsSec = s.getConfigurationSection("items");
                if (itemsSec != null) {
                    for (String key : itemsSec.getKeys(false)) {
                        try {
                            int slot = Integer.parseInt(key);
                            ConfigurationSection si = itemsSec.getConfigurationSection(key);
                            if (si == null) continue;
                            ItemStack item = si.getItemStack("item");
                            int price = si.getInt("price", 1);
                            if (item != null && slot >= 0 && slot < size) {
                                shop.getItems().put(slot, new ShopItem(item, price));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                shops.put(name.toLowerCase(java.util.Locale.ROOT), shop);
            }
        }
        ConfigurationSection linkSec = data.getConfigurationSection("npc_links");
        if (linkSec != null) {
            for (String npcName : linkSec.getKeys(false)) {
                String shopName = linkSec.getString(npcName);
                if (shopName != null) npcLinks.put(npcName, shopName);
            }
        }
    }

    public void save() {
        data.set("shops", null);
        data.set("npc_links", null);

        for (Shop shop : shops.values()) {
            String base = "shops." + shop.getName();
            data.set(base + ".size", shop.getSize());
            data.set(base + ".currency", shop.getCurrency());
            ConfigurationSection itemsSec = data.createSection(base + ".items");
            for (java.util.Map.Entry<Integer, ShopItem> e : shop.getItems().entrySet()) {
                ConfigurationSection si = itemsSec.createSection(String.valueOf(e.getKey()));
                si.set("item", e.getValue().getItem());
                si.set("price", e.getValue().getPrice());
            }
        }
        for (java.util.Map.Entry<String, String> e : npcLinks.entrySet()) data.set("npc_links." + e.getKey(), e.getValue());
        try { data.save(file); } catch (IOException ex) { plugin.getLogger().warning("shops.yml 저장 실패: " + ex.getMessage()); }
    }

    public boolean exists(String name) { return shops.containsKey(name.toLowerCase(java.util.Locale.ROOT)); }
    public Shop get(String name) { return shops.get(name.toLowerCase(java.util.Locale.ROOT)); }
    public Shop create(String name) { Shop shop = new Shop(name, 54); shops.put(name.toLowerCase(java.util.Locale.ROOT), shop); return shop; }
    public boolean delete(String name) { return shops.remove(name.toLowerCase(java.util.Locale.ROOT)) != null; }
    public java.util.Collection<Shop> all() { return shops.values(); }
    public void linkNpc(String npcName, String shopName) { npcLinks.put(npcName, shopName); }
    public void unlinkNpc(String npcName) { npcLinks.remove(npcName); }
    public String getLinkedShopName(String npcName) { return npcLinks.get(npcName); }
    public java.util.Map<String, String> getNpcLinks() { return npcLinks; }
}
