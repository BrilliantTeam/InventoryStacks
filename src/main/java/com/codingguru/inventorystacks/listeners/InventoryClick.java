package com.codingguru.inventorystacks.listeners;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.scheduler.InventoryUpdateTask;
import com.codingguru.inventorystacks.util.InventoryUtil;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.MessagesUtil;
import com.codingguru.inventorystacks.util.VersionUtil;

public class InventoryClick implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onVillagerTradingClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getCurrentItem().getAmount() <= 1)
			return;

		if (e.getInventory().getType() != InventoryType.MERCHANT)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes()
				.containsKey(e.getCurrentItem().getType()))
			return;

		e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBundleInsert(InventoryClickEvent e) {
		if (e.getClick() != ClickType.LEFT)
			return;

		ItemStack cursor = e.getCursor();
		ItemStack current = e.getCurrentItem();

		if (cursor == null || current == null)
			return;

		boolean cursorIsBundle = Tag.ITEMS_BUNDLES.isTagged(cursor.getType());
		ItemStack bundle = cursorIsBundle ? cursor : current;
		ItemStack item = cursorIsBundle ? current : cursor;

		if (!Tag.ITEMS_BUNDLES.isTagged(bundle.getType()) || item.getType() == Material.AIR)
			return;

		if (!(bundle.getItemMeta() instanceof BundleMeta meta))
			return;

		Integer defaultSize = ItemHandler.getInstance().getCachedMaterialSizes().get(item.getType());

		if (defaultSize == null || item.getMaxStackSize() <= defaultSize)
			return;

		int inBundle = meta.getItems().stream().filter(item::isSimilar).mapToInt(ItemStack::getAmount).sum();
		int allowed = defaultSize - inBundle;

		if (allowed <= 0) {
			e.setCancelled(true);
			return;
		}

		int excess = item.getAmount() - allowed;

		if (excess <= 0)
			return;

		ItemStack kept = item.clone();
		kept.setAmount(allowed);

		if (cursorIsBundle)
			e.setCurrentItem(kept);
		else
			e.getView().setCursor(kept);

		ItemStack returned = item.clone();
		returned.setAmount(excess);
		ItemUtil.addItem((Player) e.getWhoClicked(), returned);
	}

	@EventHandler(ignoreCancelled = true)
	public void onEnchantedBookClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getInventory().getType() != InventoryType.ANVIL)
			return;

		if (e.getSlotType() != SlotType.RESULT)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(Material.ENCHANTED_BOOK))
			return;

		ItemStack craftedItem = e.getInventory().getContents()[1];

		if (craftedItem == null)
			return;

		if (craftedItem.getType() != Material.ENCHANTED_BOOK)
			return;

		if (craftedItem.getAmount() <= 1)
			return;

		ItemStack newItem = craftedItem.clone();
		newItem.setAmount(craftedItem.getAmount() - 1);
		ItemUtil.addItem((Player) e.getWhoClicked(), newItem);
	}

	@EventHandler(ignoreCancelled = true)
	public void onToolStackedClick(InventoryClickEvent e) {
		if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
			return;

		if (e.getInventory().getType() != InventoryType.ANVIL)
			return;

		if (e.getSlotType() != SlotType.RESULT)
			return;

		if (!InventoryStacks.getInstance().getConfig().getBoolean("disallow-stacked-anvil-items"))
			return;

		ItemStack craftedItem = e.getInventory().getContents()[0];

		if (craftedItem.getAmount() <= 1)
			return;

		if (!ItemHandler.getInstance().getCachedMaterialSizes().containsKey(craftedItem.getType()))
			return;

		e.getWhoClicked().closeInventory();
		e.setCancelled(true);
		MessagesUtil.sendMessage(e.getWhoClicked(), MessagesUtil.DISALLOW_ANVIL_STACK.toString());
	}
}