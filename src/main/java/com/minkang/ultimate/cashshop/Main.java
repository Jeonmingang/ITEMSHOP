
package com.minkang.ultimate.cashshop;

import com.minkang.ultimate.cashshop.commands.CashCommand;
import com.minkang.ultimate.cashshop.commands.DonateShopCommand;
import com.minkang.ultimate.cashshop.commands.ShopCommand;
import com.minkang.ultimate.cashshop.listeners.GuiListener;
import com.minkang.ultimate.cashshop.store.BalanceStore;
import com.minkang.ultimate.cashshop.store.ShopStore;
import com.minkang.ultimate.cashshop.store.LinkStore;
import com.minkang.ultimate.cashshop.util.CitizenHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private BalanceStore balanceStore;
    private ShopStore shopStore;
    private LinkStore linkStore;

    public static Main getInstance() {
        return instance;
    }

    public BalanceStore getBalanceStore() {
        return balanceStore;
    }

    public ShopStore getShopStore() {
        return shopStore;
    }

    public LinkStore getLinkStore() {
        return linkStore;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        balanceStore = new BalanceStore(this);
        balanceStore.load();

        shopStore = new ShopStore(this);
        shopStore.load();

        linkStore = new LinkStore(this);
        linkStore.load();

        // Commands
        getCommand("캐시상점").setExecutor(new ShopCommand());
        getCommand("캐시").setExecutor(new CashCommand());
        getCommand("후원상점").setExecutor(new DonateShopCommand());

        // Listeners
        Bukkit.getPluginManager().registerEvents(new GuiListener(), this);

        // Citizens (optional) dynamic hook
        CitizenHook.tryRegister(this);

        getLogger().info("UltimateCashShop v1.1.0 enabled.");
    }

    @Override
    public void onDisable() {
        if (balanceStore != null) balanceStore.save();
        if (shopStore != null) shopStore.save();
        if (linkStore != null) linkStore.save();
        getLogger().info("UltimateCashShop disabled.");
    }
}
