package com.codingguru.inventorystacks.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

import java.util.EnumSet;
import java.util.Set;

public final class BlockUtil {

    private BlockUtil() {}

    public static final Set<Material> MUD_CONVERTIBLE_BLOCKS = EnumSet.noneOf(Material.class);

    static {
        MUD_CONVERTIBLE_BLOCKS.add(Material.DIRT);
        MUD_CONVERTIBLE_BLOCKS.add(Material.COARSE_DIRT);
        MUD_CONVERTIBLE_BLOCKS.add(Material.ROOTED_DIRT);
    }

    public static Block getFrontBlock(Block block) {
        if (block == null) {
            return null;
        }

        BlockData data = block.getBlockData();
        if (!(data instanceof Directional directional)) {
            return null;
        }

        BlockFace facing = directional.getFacing();
        return block.getRelative(facing);
    }

    public static BlockFace getFacing(Block block) {
        if (block == null) {
            return null;
        }

        BlockData data = block.getBlockData();
        if (!(data instanceof Directional directional)) {
            return null;
        }

        return directional.getFacing();
    }
}
