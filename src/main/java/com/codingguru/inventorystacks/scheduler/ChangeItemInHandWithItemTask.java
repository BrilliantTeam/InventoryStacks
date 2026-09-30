package com.codingguru.inventorystacks.scheduler;

import java.lang.ref.WeakReference;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.codingguru.inventorystacks.util.ItemUtil;
import com.codingguru.inventorystacks.util.VersionUtil;

public class ChangeItemInHandWithItemTask extends Schedule {

    private final WeakReference<Player> player;
    private final ItemStack item;
    private final ItemStack addedItem;
    private final Material material;
    private final EquipmentSlot hand;
    private final int slot;
    private final boolean returnItemIfMoved;

    public ChangeItemInHandWithItemTask(Player player, ItemStack item, ItemStack addedItem, Material material) {
        this(player, null, item, addedItem, material);
    }

    public ChangeItemInHandWithItemTask(Player player, EquipmentSlot hand, ItemStack item, ItemStack addedItem,
            Material material) {
        this(player, hand, item, addedItem, material, false);
    }

    public ChangeItemInHandWithItemTask(Player player, EquipmentSlot hand, ItemStack item, ItemStack addedItem,
            Material material, boolean returnItemIfMoved) {
        this.player = new WeakReference<Player>(player);
        this.hand = hand;
        this.returnItemIfMoved = returnItemIfMoved;
        if (hand == null || !VersionUtil.v1_9_R1.isServerVersionHigher()) {
            this.slot = -1;
        } else {
            this.slot = hand == EquipmentSlot.OFF_HAND ? 40 : player.getInventory().getHeldItemSlot();
        }
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
        } else if (returnItemIfMoved) {
            ItemUtil.addItem(player, item);
        }
    }

    public void runTaskLater(long delay) {
        Player player = this.player.get();
        if (player != null) {
            runTaskLater(player, delay);
        }
    }

    @SuppressWarnings("deprecation")
    private boolean updateItem(Player player, ItemStack item, Material mat) {
        if (!VersionUtil.v1_9_R1.isServerVersionHigher()) {
            player.getInventory().setItemInHand(item);
            return true;
        }

        PlayerInventory inventory = player.getInventory();

        if (slot >= 0) {
            ItemStack current = inventory.getItem(slot);
            if (current == null || current.getType() != mat || current.getAmount() != 1) {
                return false;
            }
            inventory.setItem(slot, item);
            return true;
        }

        if (hand != EquipmentSlot.OFF_HAND && inventory.getItemInMainHand().getType() == mat) {
            inventory.setItemInMainHand(item);
            return true;
        }

        if (hand != EquipmentSlot.HAND && inventory.getItemInOffHand().getType() == mat) {
            inventory.setItemInOffHand(item);
            return true;
        }

        return false;
    }
}