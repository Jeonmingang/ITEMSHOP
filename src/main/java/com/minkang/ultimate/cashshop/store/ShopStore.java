
package com.minkang.ultimate.cashshop.store;

import com.minkang.ultimate.cashshop.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShopStore {

    public static class Listing {
        public int slot;
        public int amount;
        public long price;
        public ItemStack item;
    }

    private final Main plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Map<Integer, Listing> listings = new HashMap<>();

    public ShopStore(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shop.yml");
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        listings.clear();
        ConfigurationSection sec = cfg.getConfigurationSection("listings");
        if (sec != null) {
            for (String k : sec.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(k);
                    ConfigurationSection s = sec.getConfigurationSection(k);
                    if (s == null) continue;
                    Listing l = new Listing();
                    l.slot = slot;
                    l.amount = s.getInt("amount", 1);
                    l.price = s.getLong("price", 0L);
                    l.item = s.getItemStack("item");
                    if (l.item != null) {
                        listings.put(slot, l);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    public void save() {
        if (cfg == null) cfg = new YamlConfiguration();
        cfg.set("listings", null);
        for (Map.Entry<Integer, Listing> e : listings.entrySet()) {
            String path = "listings." + e.getKey();
            Listing l = e.getValue();
            cfg.set(path + ".amount", l.amount);
            cfg.set(path + ".price", l.price);
            cfg.set(path + ".item", l.item);
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<Listing> get(int slot) {
        return Optional.ofNullable(listings.get(slot));
    }

    public void put(int slot, ItemStack item, int amount, long price) {
        Listing l = new Listing();
        l.slot = slot;
        l.amount = Math.max(1, amount);
        l.price = Math.max(0L, price);
        l.item = item.clone();
        listings.put(slot, l);
        save();
    }

    public void remove(int slot) {
        listings.remove(slot);
        save();
    }

    public Map<Integer, Listing> all() {
        return listings;
    }
}
