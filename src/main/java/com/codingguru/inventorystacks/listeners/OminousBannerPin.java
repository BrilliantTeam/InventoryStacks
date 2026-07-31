package com.codingguru.inventorystacks.listeners;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Raider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.TranslatableComponent;

public class OminousBannerPin implements Listener {

    private static final int VANILLA_BANNER_STACK_SIZE = 16;

    private static final String OMINOUS_NAME_KEY = "block.minecraft.ominous_banner";

    private static final List<Pattern> OMINOUS_PATTERNS = List.of(
            new Pattern(DyeColor.CYAN, PatternType.RHOMBUS),
            new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM),
            new Pattern(DyeColor.GRAY, PatternType.STRIPE_CENTER),
            new Pattern(DyeColor.LIGHT_GRAY, PatternType.BORDER),
            new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE),
            new Pattern(DyeColor.LIGHT_GRAY, PatternType.HALF_HORIZONTAL),
            new Pattern(DyeColor.LIGHT_GRAY, PatternType.CIRCLE),
            new Pattern(DyeColor.BLACK, PatternType.BORDER));

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (!(dead instanceof Raider)) return;

        EntityEquipment equipment = dead.getEquipment();
        ItemStack helmet = equipment == null ? null : equipment.getHelmet();
        if (!isOminousBanner(helmet)) return;

        ItemMeta meta = helmet.getItemMeta();
        if (meta.hasMaxStackSize() && meta.getMaxStackSize() == VANILLA_BANNER_STACK_SIZE) return;

        ItemStack pinned = helmet.clone();
        ItemMeta pinnedMeta = pinned.getItemMeta();
        pinnedMeta.setMaxStackSize(VANILLA_BANNER_STACK_SIZE);
        pinned.setItemMeta(pinnedMeta);
        equipment.setHelmet(pinned, true);
    }

    private static boolean isOminousBanner(ItemStack helmet) {
        return helmet != null
                && helmet.getType() == Material.WHITE_BANNER
                && helmet.getItemMeta() instanceof BannerMeta meta
                && meta.getPatterns().equals(OMINOUS_PATTERNS)
                && meta.hasItemName()
                && meta.itemName() instanceof TranslatableComponent name
                && name.key().equals(OMINOUS_NAME_KEY);
    }
}
