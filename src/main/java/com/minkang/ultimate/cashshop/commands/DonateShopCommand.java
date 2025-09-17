
package com.minkang.ultimate.cashshop.commands;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.store.ShopStore;
import com.minkang.ultimate.cashshop.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DonateShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ItemUtil.color(Main.getInstance().getConfig().getString("messages.only_player", "&c플레이어만 사용 가능")));
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("ultimate.cashshop.admin")) {
            p.sendMessage("§c권한이 없습니다.");
            return true;
        }
        if (args.length == 0) {
            p.sendMessage(ItemUtil.color("&c사용법: " + command.getUsage()));
            return true;
        }

        if ("등록".equalsIgnoreCase(args[0])) {
            if (args.length < 4) {
                p.sendMessage(ItemUtil.color("&c사용법: /후원상점 등록 <갯수> <가격(캐시)> <슬롯>"));
                return true;
            }
            int amount;
            long price;
            int slot;
            try {
                amount = Integer.parseInt(args[1]);
                price = Long.parseLong(args[2]);
                slot = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                p.sendMessage("§c숫자 형식이 올바르지 않습니다.");
                return true;
            }
            if (slot < 0 || slot > 53) {
                p.sendMessage(ItemUtil.color(Main.getInstance().getConfig().getString("messages.invalid_slot")));
                return true;
            }
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                p.sendMessage(ItemUtil.color(Main.getInstance().getConfig().getString("messages.hold_item")));
                return true;
            }
            Main.getInstance().getShopStore().put(slot, hand, amount, price);
            Map<String, String> repl = new HashMap<>();
            repl.put("slot", Integer.toString(slot));
            repl.put("amount", Integer.toString(amount));
            repl.put("price", Long.toString(price));
            p.sendMessage(ItemUtil.fmt(Main.getInstance().getConfig().getString("messages.registered"), repl));
            return true;
        }

        if ("등록취소".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                p.sendMessage(ItemUtil.color("&c사용법: /후원상점 등록취소 <슬롯>"));
                return true;
            }
            int slot;
            try {
                slot = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage("§c숫자 형식이 올바르지 않습니다.");
                return true;
            }
            if (slot < 0 || slot > 53) {
                p.sendMessage(ItemUtil.color(Main.getInstance().getConfig().getString("messages.invalid_slot")));
                return true;
            }
            Main.getInstance().getShopStore().remove(slot);
            Map<String, String> repl = new HashMap<>();
            repl.put("slot", Integer.toString(slot));
            p.sendMessage(ItemUtil.fmt(Main.getInstance().getConfig().getString("messages.unregistered"), repl));
            return true;
        }

        p.sendMessage(ItemUtil.color("&c사용법: " + command.getUsage()));
        return true;
    }
}
