package com.codingguru.inventorystacks.scheduler;

import java.lang.ref.WeakReference;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.VersionUtil;

public class ChangeItemInHandWithItemTask extends Schedule {

    private final WeakReference<Player> player;
    private final ItemStack item;
    private final ItemStack addedItem;
    private final Material material;

    public ChangeItemInHandWithItemTask(Player player, ItemStack item, ItemStack addedItem, Material material) {
        this.player = new WeakReference<Player>(player);
        this.item = item;
        this.addedItem = addedItem;
        this.material = material;
    }

    @Override
    public void run() {
        Player player = this.player.get();
        if (player == null) return;
        boolean replacedHand = updateItem(player, item, material);
        if (replacedHand) {
            ItemUtil.addItem(player, addedItem);
        }
    }

    @SuppressWarnings("deprecation")
    private boolean updateItem(Player player, ItemStack item, Material mat) {
        if (!VersionUtil.v1_9_R1.isServerVersionHigher()) {
            player.getInventory().setItemInHand(item);
            return true;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (mainHand.getType() == mat) {
            player.getInventory().setItemInMainHand(item);
            return true;
        } else if (offHand.getType() == mat) {
            player.getInventory().setItemInOffHand(item);
            return true;
        } else {
            ItemUtil.addItem(player, item);
            return false;
        }
    }
}