package com.codingguru.inventorystacks.listeners;

import com.codingguru.inventorystacks.scheduler.RegionSchedule;
import com.codingguru.inventorystacks.util.BlockUtil;
import com.codingguru.inventorystacks.util.XMaterialUtil;
import io.papermc.paper.event.block.BlockPreDispenseEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.codingguru.inventorystacks.util.VersionUtil;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class BlockDispense implements Listener {

    private final long itemChangeDelay;

    public BlockDispense(long itemChangeDelay) {
        this.itemChangeDelay = itemChangeDelay;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPreDispense(BlockPreDispenseEvent e) {
        if (!VersionUtil.v1_19_R1.isServerVersionHigher()) return;

        final Block block = e.getBlock();
        final ItemStack eventItem = e.getItemStack();

        if (eventItem.getType() != Material.POTION) return;

        if (!(eventItem.getItemMeta() instanceof PotionMeta meta)) return;
        if (meta.getBasePotionType() != PotionType.WATER) return;

        if (eventItem.getAmount() <= 1) return;

        e.setCancelled(true);

        final int slot = e.getSlot();
        final ItemStack expectedPotion = eventItem.clone();

        new RegionSchedule() {
            @Override
            public void run() {

                if (block.getType() != Material.DISPENSER) return;

                // 獲取前方方塊
                final Block front = BlockUtil.getFrontBlock(block);
                if (front == null) return;

                // 獲取是否可以被轉換
                if (!BlockUtil.MUD_CONVERTIBLE_BLOCKS.contains(front.getType())) return;

                // 拿方塊包包
                if (!(block.getState() instanceof Dispenser dispenser)) return;
                final Inventory inv = dispenser.getInventory();

                // 看看有沒有在這一個 tick 被更動？ (不應該有)
                ItemStack current = inv.getItem(slot);
                if (current == null) return;
                if (!current.isSimilar(expectedPotion)) return;

                // 拿物品數量
                int amount = current.getAmount();
                if (amount <= 1) return;

                // 模仿原版
                front.setType(Material.MUD);

                // 還回去垃圾，還不回去就隨手亂丟垃圾
                Material glassBottle = XMaterialUtil.GLASS_BOTTLE.parseMaterial();
                if (glassBottle != null) {
                    ItemStack bottle = new ItemStack(glassBottle, 1);
                    if (inv.firstEmpty() == -1) {
                        block.getWorld().dropItem(block.getLocation(), bottle);
                    } else {
                        inv.addItem(bottle);
                    }
                }

                // 複製一個新的 ItemStack，數量減少 1
                int newAmount = amount - 1;
                ItemStack cloned = current.clone();
                cloned.setAmount(newAmount);
                inv.setItem(slot, cloned);

                block.getState().update(true);
            }
        }.runTaskLaterAt(block, itemChangeDelay);
    }
}