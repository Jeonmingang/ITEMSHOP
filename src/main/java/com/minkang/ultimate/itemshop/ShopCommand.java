package com.minkang.ultimate.itemshop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public ShopCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e/아이템상점 생성 <이름>");
            sender.sendMessage("§e/아이템상점 연동 <NPC이름> [상점이름]");
            sender.sendMessage("§e/아이템상점 연결해제 <NPC이름>");
            sender.sendMessage("§e/아이템상점 연결목록");
            sender.sendMessage("§e/아이템상점 삭제 <이름>");
            sender.sendMessage("§e/아이템상점 설정 <이름>  §7(현재 손에 든 아이템을 화폐로 설정)");
            sender.sendMessage("§e/아이템상점 목록");
            sender.sendMessage("§e/아이템상점 추가 <이름> <슬롯(0~53)> <가격(화폐수)>  §7(손에 든 아이템 등록)");
            sender.sendMessage("§e/아이템상점 제거 <이름> <슬롯>");
            sender.sendMessage("§e/아이템상점 열기 <이름>");
            sender.sendMessage("§e/아이템상점 리로드");
            return true;
        }

        String sub = args[0];

        if ("리로드".equals(sub)) {
            if (!sender.hasPermission("ultimate.itemshop.reload") && !sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            plugin.getShopManager().save();
            plugin.reloadFromDisk();
            sender.sendMessage("§aUltimateItemShop 설정 및 데이터 리로드 완료.");
            return true;
        }

        if ("열기".equals(sub)) {
            if (args.length < 2) {
                sender.sendMessage("§c사용법: /아이템상점 열기 <이름>");
                return true;
            }
            String name = args[1];
            Shop shop = plugin.getShopManager().get(name);
            if (shop == null) {
                sender.sendMessage("§c존재하지 않는 상점입니다.");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c플레이어만 사용할 수 있습니다.");
                return true;
            }
            Player p = (Player) sender;
            p.openInventory(shop.createInventory());
            return true;
        }

        if ("연결목록".equals(sub)) {
            if (!sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            java.util.Map<String,String> links = plugin.getShopManager().getNpcLinks();
            if (links.isEmpty()) {
                sender.sendMessage("§7연결된 NPC가 없습니다.");
            } else {
                sender.sendMessage("§aNPC 연결 목록:");
                for (java.util.Map.Entry<String,String> e : links.entrySet()) {
                    sender.sendMessage(" §7- §f" + e.getKey() + " §7→ §f" + e.getValue());
                }
            }
            return true;
        }

        if ("연결해제".equals(sub)) {
            if (!sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§c사용법: /아이템상점 연결해제 <NPC이름>");
                return true;
            }
            String npcName = args[1];
            if (plugin.getShopManager().getNpcLinks().containsKey(npcName)) {
                plugin.getShopManager().unlinkNpc(npcName);
                plugin.getShopManager().save();
                sender.sendMessage("§a연결 해제 완료: " + npcName);
            } else {
                sender.sendMessage("§c해당 NPC 이름의 연결이 없습니다.");
            }
            return true;
        }

        if ("생성".equals(sub)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c플레이어만 사용 가능합니다.");
                return true;
            }
            if (!sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§c사용법: /아이템상점 생성 <이름>");
                return true;
            }
            String name = args[1];
            if (plugin.getShopManager().exists(name)) {
                sender.sendMessage("§c이미 존재하는 상점입니다.");
                return true;
            }
            Shop shop = plugin.getShopManager().create(name);
            plugin.getShopManager().save();
            sender.sendMessage("§a상점 생성 완료: " + name + " §7(기본 54칸)");
            if (sender instanceof Player) {
                ((Player) sender).openInventory(shop.createInventory());
            }
            return true;
        }

        if ("연동".equals(sub)) {
            if (!sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§c사용법: /아이템상점 연동 <NPC이름> [상점이름]");
                return true;
            }
            String npcName = args[1];
            String shopName;
            if (args.length >= 3) {
                shopName = args[2];
            } else {
                if (plugin.getShopManager().exists(npcName)) {
                    shopName = npcName;
                } else {
                    sender.sendMessage("§c상점 이름을 지정해주세요: /아이템상점 연동 <NPC이름> <상점이름>");
                    return true;
                }
            }
            if (!plugin.getShopManager().exists(shopName)) {
                sender.sendMessage("§c해당 상점이 없습니다: " + shopName);
                return true;
            }
            plugin.getShopManager().linkNpc(npcName, shopName);
            plugin.getShopManager().save();
            sender.sendMessage("§a연동 완료: NPC '" + npcName + "' → 상점 '" + shopName + "'");
            return true;
        }

        if ("삭제".equals(sub)) {
            if (!sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§c사용법: /아이템상점 삭제 <이름>");
                return true;
            }
            String name = args[1];
            if (!plugin.getShopManager().exists(name)) {
                sender.sendMessage("§c존재하지 않는 상점입니다.");
                return true;
            }
            boolean ok = plugin.getShopManager().delete(name);
            if (!ok) {
                sender.sendMessage("§c삭제 실패");
                return true;
            }
            java.util.List<String> toUnlink = new java.util.ArrayList<String>();
            for (java.util.Map.Entry<String,String> e : plugin.getShopManager().getNpcLinks().entrySet()) {
                if (e.getValue().equalsIgnoreCase(name)) toUnlink.add(e.getKey());
            }
            for (String k : toUnlink) plugin.getShopManager().unlinkNpc(k);
            plugin.getShopManager().save();
            sender.sendMessage("§a상점 삭제 완료: " + name);
            return true;
        }

        if ("설정".equals(sub)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c플레이어만 사용 가능합니다.");
                return true;
            }
            if (!sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage("§c사용법: /아이템상점 설정 <이름>");
                return true;
            }
            String name = args[1];
            Shop shop = plugin.getShopManager().get(name);
            if (shop == null) {
                sender.sendMessage("§c존재하지 않는 상점입니다.");
                return true;
            }
            Player p = (Player) sender;
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == org.bukkit.Material.AIR) {
                sender.sendMessage("§c손에 아이템을 들고 사용해주세요.");
                return true;
            }
            ItemStack currency = hand.clone();
            currency.setAmount(1);
            shop.setCurrency(currency);
            plugin.getShopManager().save();
            sender.sendMessage("§a화폐 설정 완료: " + (currency.getItemMeta()!=null && currency.getItemMeta().hasDisplayName()
                    ? ChatColor.stripColor(currency.getItemMeta().getDisplayName())
                    : currency.getType().name()));
            return true;
        }

        if ("목록".equals(sub)) {
            StringBuilder sb = new StringBuilder("§a상점 목록: ");
            boolean first = true;
            for (Shop s : plugin.getShopManager().all()) {
                if (!first) sb.append("§7, ");
                sb.append("§f").append(s.getName());
                first = false;
            }
            if (first) sb.append("§7(없음)");
            sender.sendMessage(sb.toString());
            return true;
        }

        if ("추가".equals(sub)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c플레이어만 사용 가능합니다.");
                return true;
            }
            if (!sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 4) {
                sender.sendMessage("§c사용법: /아이템상점 추가 <이름> <슬롯(0~53)> <가격>");
                return true;
            }
            String name = args[1];
            Shop shop = plugin.getShopManager().get(name);
            if (shop == null) {
                sender.sendMessage("§c존재하지 않는 상점입니다.");
                return true;
            }
            int slot;
            try {
                slot = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                sender.sendMessage("§c슬롯은 숫자여야 합니다. (0~53)");
                return true;
            }
            if (slot < 0 || slot >= shop.getSize()) {
                sender.sendMessage("§c슬롯 범위: 0~" + (shop.getSize()-1));
                return true;
            }
            int price;
            try {
                price = Integer.parseInt(args[3]);
            } catch (NumberFormatException ex) {
                sender.sendMessage("§c가격은 숫자여야 합니다.");
                return true;
            }
            Player p = (Player) sender;
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == org.bukkit.Material.AIR) {
                sender.sendMessage("§c손에 등록할 아이템을 들고 사용해주세요.");
                return true;
            }
            ItemStack toSave = hand.clone();
            shop.getItems().put(slot, new ShopItem(toSave, price));
            plugin.getShopManager().save();
            sender.sendMessage("§a아이템 등록 완료: 슬롯 " + slot + " 가격 " + price);
            return true;
        }

        if ("제거".equals(sub)) {
            if (!sender.hasPermission("ultimate.itemshop.admin")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage("§c사용법: /아이템상점 제거 <이름> <슬롯>");
                return true;
            }
            String name = args[1];
            Shop shop = plugin.getShopManager().get(name);
            if (shop == null) {
                sender.sendMessage("§c존재하지 않는 상점입니다.");
                return true;
            }
            int slot;
            try {
                slot = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                sender.sendMessage("§c슬롯은 숫자여야 합니다.");
                return true;
            }
            if (shop.getItems().remove(slot) != null) {
                plugin.getShopManager().save();
                sender.sendMessage("§a슬롯 " + slot + " 아이템 제거 완료.");
            } else {
                sender.sendMessage("§c해당 슬롯에는 등록된 아이템이 없습니다.");
            }
            return true;
        }

        sender.sendMessage("§c알 수 없는 하위 명령어입니다.");
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        java.util.List<String> empty = new java.util.ArrayList<String>();
        if (args.length == 1) {
            java.util.List<String> base = java.util.Arrays.asList("생성","연동","연결해제","연결목록","삭제","설정","목록","추가","제거","열기","리로드");
            String a = args[0];
            java.util.List<String> out = new java.util.ArrayList<String>();
            for (String s : base) if (s.startsWith(a)) out.add(s);
            return out;
        }
        if (args.length == 2) {
            String sub = args[0];
            if (sub.equals("삭제") || sub.equals("설정") || sub.equals("추가") || sub.equals("제거") || sub.equals("열기")) {
                java.util.List<String> names = new java.util.ArrayList<String>();
                for (Shop s : plugin.getShopManager().all()) names.add(s.getName());
                java.util.List<String> out = new java.util.ArrayList<String>();
                String a = args[1];
                for (String s : names) if (s.startsWith(a)) out.add(s);
                return out;
            }
        }
        return empty;
    }
}
