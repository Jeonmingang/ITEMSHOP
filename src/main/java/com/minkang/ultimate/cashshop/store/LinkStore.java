
package com.minkang.ultimate.cashshop.store;

import com.minkang.ultimate.cashshop.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinkStore {

    private final Main plugin;
    private final File file;
    private FileConfiguration cfg;
    private final Set<Integer> linked = new HashSet<>();

    public LinkStore(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "links.yml");
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
        linked.clear();
        List<Integer> list = cfg.getIntegerList("npc_ids");
        linked.addAll(list);
    }

    public void save() {
        if (cfg == null) cfg = new YamlConfiguration();
        cfg.set("npc_ids", linked.toArray(new Integer[0]));
        try {
            cfg.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isLinked(int id) {
        return linked.contains(id);
    }

    public boolean add(int id) {
        boolean added = linked.add(id);
        if (added) save();
        return added;
    }

    public boolean remove(int id) {
        boolean removed = linked.remove(id);
        if (removed) save();
        return removed;
    }
}
