package com.codingguru.inventorystacks.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.scheduler.ChangeItemInHandWithItemTask;
import com.codingguru.inventorystacks.util.VersionUtil;

public class PlayerInteractEntity implements Listener {

    private final long itemChangeDelay;

    public PlayerInteractEntity(long itemChangeDelay) {
        this.itemChangeDelay = itemChangeDelay;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!VersionUtil.v1_17_R1.isServerVersionHigher()) return;

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (!(e.getRightClicked() instanceof Axolotl))
            return;

        if (e.getRightClicked() instanceof Animals animals) {
            if (!animals.canBreed())
                return;
            if (animals.isLoveMode())
                return;
        }

        Player player = e.getPlayer();
        ItemStack holding;

        if (e.getHand() == EquipmentSlot.HAND) {
            holding = player.getInventory().getItemInMainHand();
        } else {
            holding = player.getInventory().getItemInOffHand();
        }

        Material type = holding.getType();

        if (type != Material.TROPICAL_FISH_BUCKET)
            return;

        int amount = holding.getAmount();

        if (amount <= 1)
            return;

        ItemStack clone = holding.clone();
        clone.setAmount(amount - 1);

        ChangeItemInHandWithItemTask changeItemTask = new ChangeItemInHandWithItemTask(
                player,
                clone,
                new ItemStack(Material.WATER_BUCKET, 1),
                Material.WATER_BUCKET
        );
        changeItemTask.runTaskLater(itemChangeDelay);
    }
}