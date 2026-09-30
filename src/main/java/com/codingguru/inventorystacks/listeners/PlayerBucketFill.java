package com.codingguru.inventorystacks.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.codingguru.inventorystacks.scheduler.InventoryUpdateTask;

public class PlayerBucketFill implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerBucketFill(PlayerBucketFillEvent e) {
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;

		new InventoryUpdateTask(e.getPlayer()).runTaskLater(e.getPlayer(), 1L);
	}
}
