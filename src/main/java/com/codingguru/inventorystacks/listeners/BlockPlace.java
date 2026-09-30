package com.codingguru.inventorystacks.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.scheduler.ChangeItemInHandWithItemTask;
import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class BlockPlace implements Listener {

	private static final Field ITEM_LEFT_IN_HAND;
	private static final Method AS_NMS_COPY;

	static {
		Field field = null;
		Method method = null;
		try {
			field = Class.forName("net.minecraft.world.item.BucketItem")
					.getDeclaredField("itemLeftInHandAfterPlayerBucketEmptyEvent");
			field.setAccessible(true);
			method = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack").getMethod("asNMSCopy",
					ItemStack.class);
		} catch (ReflectiveOperationException | RuntimeException e) {
			field = null;
		}
		ITEM_LEFT_IN_HAND = field;
		AS_NMS_COPY = method;
	}

	private final long itemChangeDelay;

	public BlockPlace(long itemChangeDelay) {
		this.itemChangeDelay = itemChangeDelay;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (!VersionUtil.v1_17_R1.isServerVersionHigher())
			return;

		Player player = e.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		ItemStack holding;

		if (e.getHand() == EquipmentSlot.HAND) {
			holding = player.getInventory().getItemInMainHand();
		} else {
			holding = player.getInventory().getItemInOffHand();
		}

		if (holding.getType() != XMaterialUtil.POWDER_SNOW_BUCKET.parseMaterial())
			return;

		int amount = holding.getAmount();

		if (amount <= 1)
			return;

		ItemStack clone = holding.clone();
		clone.setAmount(amount - 1);

		if (setItemLeftInHand(clone)) {
			ItemUtil.addItem(player, new ItemStack(XMaterialUtil.BUCKET.parseMaterial()));
			return;
		}

		ChangeItemInHandWithItemTask changeItemTask = new ChangeItemInHandWithItemTask(e.getPlayer(), e.getHand(), clone,
				new ItemStack(XMaterialUtil.BUCKET.parseMaterial()), XMaterialUtil.BUCKET.parseMaterial(), true);
		changeItemTask.runTaskLater(itemChangeDelay);
	}

	private static boolean setItemLeftInHand(ItemStack item) {
		if (ITEM_LEFT_IN_HAND == null)
			return false;

		try {
			ITEM_LEFT_IN_HAND.set(null, AS_NMS_COPY.invoke(null, item));
			return true;
		} catch (ReflectiveOperationException | RuntimeException e) {
			return false;
		}
	}
}
