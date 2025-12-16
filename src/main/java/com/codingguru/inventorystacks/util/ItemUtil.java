package com.codingguru.inventorystacks.util;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ItemUtil {

	private static boolean usingFolia() {
		return ItemHandler.getInstance().getServerType() == ServerTypeUtil.FOLIA;
	}

	public static boolean addItemToBrewingStand(Inventory inventory, ItemStack item) {
		ItemStack slot1 = inventory.getItem(0);
		ItemStack slot2 = inventory.getItem(1);
		ItemStack slot3 = inventory.getItem(2);

		if (slot1 == null || slot1.getType() == Material.AIR) {
			inventory.setItem(0, item);
			return true;
		} else if (slot2 == null || slot2.getType() == Material.AIR) {
			inventory.setItem(1, item);
			return true;
		} else if (slot3 == null || slot3.getType() == Material.AIR) {
			inventory.setItem(2, item);
			return true;
		}
		
		return false;
	}

	public static void addItem(Player player, ItemStack item) {
		if (player == null || item == null || item.getType() == Material.AIR || item.getAmount() <= 0) {
			return;
		}

		int amount = item.getAmount();
		int maxAmount = item.getMaxStackSize();

		if (amount > maxAmount) {
			item.setAmount(maxAmount);
			add(player, item);

			for (int i = amount - maxAmount; i >= maxAmount; i -= maxAmount) {
				add(player, item);
			}

			if (amount % maxAmount > 0) {
				item.setAmount(amount % maxAmount);
				add(player, item);
			}
		} else {
			add(player, item);
		}
	}

	private static void add(Player player, ItemStack item) {
		InventoryStacks plugin = InventoryStacks.getInstance();

		if (usingFolia()) {
			player.getScheduler().run(plugin, task -> addInternal(player, item), null);
		} else {
			Bukkit.getScheduler().runTask(plugin, () -> addInternal(player, item));
		}
	}

	private static void addInternal(Player player, ItemStack item) {
		if (!player.isOnline()) {
			return;
		}

		Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
		if (leftover.isEmpty()) {
			return;
		}

		for (ItemStack lf : leftover.values()) {
			if (lf == null || lf.getType() == Material.AIR || lf.getAmount() <= 0) {
				continue;
			}
			player.getWorld().dropItemNaturally(player.getLocation(), lf);
		}
	}

}