package com.codingguru.inventorystacks.listeners;

import org.bukkit.Material;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.VersionUtil;

public class PlayerInteract implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent e) {
		//if (VersionUtil.v1_20_R4.isServerVersionHigher())
		//	return;

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (e.getClickedBlock().getType() != Material.JUKEBOX)
			return;

		if (e.getItem() == null || e.getItem().getType() == Material.AIR)
			return;

		ItemStack holding = e.getItem();

		if (!isMusicDisc(holding.getType()))
			return;

		int amount = holding.getAmount();

		if (amount <= 1)
			return;

		Jukebox jukebox = (Jukebox) e.getClickedBlock().getState();

		if (jukebox.hasRecord())
			return;

		e.setCancelled(true);
		holding.setAmount(holding.getAmount() - 1);
		ItemStack clone = holding.clone();
		clone.setAmount(1);
		jukebox.setRecord(clone);
		jukebox.update();
	}

	private boolean isMusicDisc(Material type) {
		return type == Material.MUSIC_DISC_11
				|| type == Material.MUSIC_DISC_13
				|| type == Material.MUSIC_DISC_5
				|| type == Material.MUSIC_DISC_BLOCKS
				|| type == Material.MUSIC_DISC_CAT
				|| type == Material.MUSIC_DISC_CHIRP
				|| type == Material.MUSIC_DISC_CREATOR
				|| type == Material.MUSIC_DISC_CREATOR_MUSIC_BOX
				|| type == Material.MUSIC_DISC_FAR
				|| type == Material.MUSIC_DISC_LAVA_CHICKEN
				|| type == Material.MUSIC_DISC_MALL
				|| type == Material.MUSIC_DISC_MELLOHI
				|| type == Material.MUSIC_DISC_OTHERSIDE
				|| type == Material.MUSIC_DISC_PIGSTEP
				|| type == Material.MUSIC_DISC_PRECIPICE
				|| type == Material.MUSIC_DISC_RELIC
				|| type == Material.MUSIC_DISC_STAL
				|| type == Material.MUSIC_DISC_STRAD
				|| type == Material.MUSIC_DISC_TEARS
				|| type == Material.MUSIC_DISC_WAIT
				|| type == Material.MUSIC_DISC_WARD;
	}
}