
package com.minkang.ultimate.cashshop.commands;

import com.minkang.ultimate.cashshop.Main;
import com.minkang.ultimate.cashshop.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CashCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                long bal = Main.getInstance().getBalanceStore().get(p.getUniqueId());
                String tpl = Main.getInstance().getConfig().getString("messages.balance", "&a보유 캐시: &f{balance}");
                Map<String, String> repl = new HashMap<>();
                repl.put("balance", Long.toString(bal));
                p.sendMessage(ItemUtil.fmt(tpl, repl));
                return true;
            } else {
                sender.sendMessage("플레이어만 사용할 수 있습니다.");
                return true;
            }
        }

        // /캐시 지급 <플레이어> <수량>
        if ("지급".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("ultimate.cashshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ItemUtil.color("&c사용법: /캐시 지급 <플레이어> <수량>"));
                return true;
            }
            String name = args[1];
            String amtStr = args[2];
            long amount;
            try {
                amount = Long.parseLong(amtStr);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c수량은 숫자여야 합니다.");
                return true;
            }
            if (amount == 0L) {
                sender.sendMessage("§c0원은 지급할 수 없습니다.");
                return true;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            UUID uuid = target.getUniqueId();
            Main.getInstance().getBalanceStore().add(uuid, amount);

            long bal = Main.getInstance().getBalanceStore().get(uuid);

            Map<String, String> repl = new HashMap<>();
            repl.put("player", target.getName() == null ? name : target.getName());
            repl.put("amount", Long.toString(amount));
            repl.put("balance", Long.toString(bal));

            String given = Main.getInstance().getConfig().getString("messages.given");
            String received = Main.getInstance().getConfig().getString("messages.received");
            if (given != null) sender.sendMessage(ItemUtil.fmt(given, repl));

            if (target.isOnline()) {
                Player tp = target.getPlayer();
                if (tp != null && received != null) {
                    tp.sendMessage(ItemUtil.fmt(received, repl));
                }
            }
            return true;
        }

        // Fallback
        sender.sendMessage(ItemUtil.color("&c사용법: /캐시 | /캐시 지급 <플레이어> <수량>"));
        return true;
    }
}
