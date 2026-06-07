package com.codingguru.inventorystacks.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.scheduler.ChangeItemInHandWithItemTask;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;

public class PlayerBucketEmpty implements Listener {

    private final long itemChangeDelay;
    
    public PlayerBucketEmpty(long itemChangeDelay) {
        this.itemChangeDelay = itemChangeDelay;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();
        
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        ItemStack holding;

        if (e.getHand() == EquipmentSlot.HAND) {
            holding = player.getInventory().getItemInMainHand();
        } else {
            holding = player.getInventory().getItemInOffHand();
        }

        if (holding.getAmount() <= 1) {
            return;
        }

        if (VersionUtil.v1_21_R1.isServerVersionHigher()) {
            return;
        }

        Material bucketMat = XMaterialUtil.BUCKET.parseMaterial();
        ItemStack clone = holding.clone();
        clone.setAmount(holding.getAmount() - 1);

        ChangeItemInHandWithItemTask changeItemTask = new ChangeItemInHandWithItemTask(
            player,
            clone,
            new ItemStack(bucketMat),
            bucketMat
        );
        changeItemTask.runTaskLater(itemChangeDelay);
    }
}