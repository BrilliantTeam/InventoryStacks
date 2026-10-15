package com.codingguru.inventorystacks.util;

import com.codingguru.inventorystacks.handlers.ItemHandler;

public enum VersionUtil {

	v26_2_R1("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "getDescriptionId", 99, 37),
	v26_3_R1("net.minecraft.world.item.Item", "net.minecraft.world.item.Items", "getDescriptionId", 99, 38);

	private final String itemClass;
	private final String itemsClass;
	private final String itemNameMethod;
	private final int absoluteMaxStackSize;
	private final int index;

	VersionUtil(String itemClass, String itemsClass, String itemNameMethod, int absoluteMaxStackSize, int index) {
		this.itemClass = itemClass;
		this.itemsClass = itemsClass;
		this.itemNameMethod = itemNameMethod;
		this.absoluteMaxStackSize = absoluteMaxStackSize;
		this.index = index;
	}

	public String getItemClass() {
		return itemClass;
	}

	public String getItemsClass() {
		return itemsClass;
	}

	public String getItemNameMethod() {
		return itemNameMethod;
	}

	public int getAbsoluteMaxStackSize() {
		return absoluteMaxStackSize;
	}

	public int getValue() {
		return index;
	}

	public boolean isServerVersionHigher() {
		return ItemHandler.getInstance().getServerVersion().getValue() >= getValue();
	}

	@Override
	public String toString() {
		return name();
	}
}
