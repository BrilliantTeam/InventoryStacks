package com.codingguru.inventorystacks.scheduler;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;
import com.codingguru.inventorystacks.util.ServerTypeUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public abstract class RegionSchedule implements Runnable {

    private final boolean USING_FOLIA = ItemHandler.getInstance().getServerType() == ServerTypeUtil.FOLIA;

    public void runTaskAt(Block block) {
        if (USING_FOLIA) {
            Bukkit.getRegionScheduler().execute(
                    InventoryStacks.getInstance(),
                    block.getLocation(),
                    this
            );
        } else {
            Bukkit.getScheduler().runTask(InventoryStacks.getInstance(), this);
        }
    }
    public void runTaskLaterAt(Block block, long delayTicks) {
        if (USING_FOLIA) {
            Bukkit.getRegionScheduler().runDelayed(
                    InventoryStacks.getInstance(),
                    block.getLocation(),
                    task -> this.run(),
                    delayTicks
            );
        } else {
            Bukkit.getScheduler().runTaskLater(
                    InventoryStacks.getInstance(),
                    this,
                    delayTicks
            );
        }
    }
}
