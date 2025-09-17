
package com.minkang.ultimate.cashshop.store;

import com.minkang.ultimate.cashshop.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BalanceStore {

    private final Main plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Map<UUID, Long> balances = new HashMap<>();

    public BalanceStore(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "balances.yml");
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
        balances.clear();
        if (cfg.isConfigurationSection("balances")) {
            for (String key : cfg.getConfigurationSection("balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long amt = cfg.getLong("balances." + key, 0L);
                    balances.put(uuid, amt);
                } catch (Exception ignored) {}
            }
        }
    }

    public void save() {
        if (cfg == null) cfg = new YamlConfiguration();
        for (Map.Entry<UUID, Long> e : balances.entrySet()) {
            cfg.set("balances." + e.getKey().toString(), e.getValue());
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long get(UUID uuid) {
        return balances.getOrDefault(uuid, 0L);
    }

    public void set(UUID uuid, long amt) {
        balances.put(uuid, Math.max(0L, amt));
    }

    public void add(UUID uuid, long delta) {
        long now = get(uuid);
        long next = now + delta;
        if (next < 0L) next = 0L;
        set(uuid, next);
    }
}
