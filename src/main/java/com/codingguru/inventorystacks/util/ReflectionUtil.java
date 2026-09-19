package com.codingguru.inventorystacks.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.handlers.ItemHandler;

public final class ReflectionUtil {
	private static final boolean MATERIAL_HAS_MAX_STACK = hasField(Material.class, "maxStack");

	private static boolean hasField(Class<?> clas, String fieldName) {
		try {
			clas.getDeclaredField(fieldName);
			return true;
		} catch (NoSuchFieldException | SecurityException e) {
			return false;
		}
	}

	public static int getDefaultStackValue(Class<?> clas, Material mat, String fieldName, String name) {
		if (clas == Material.class && !MATERIAL_HAS_MAX_STACK) {
			return mat.getMaxStackSize();
		}

		int defaultSize = 1; // More safe than using 64 as could be tools, armour, etc

		Field currentField;

		try {
			currentField = clas.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to retreive the max item stack size of: " + name + ".");
			return defaultSize;
		}

		currentField.setAccessible(true);

		try {
			defaultSize = currentField.getInt(mat);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to retreive the max item stack size of: " + name + ".");
			defaultSize = 1;
			return defaultSize;
		}

		currentField.setAccessible(false);

		return defaultSize;
	}

	public static boolean setClassField(Class<?> clas, Object instance, String fieldName, int value, String name) {
		if (clas == Material.class && !MATERIAL_HAS_MAX_STACK) {
			return true;
		}

		Field currentField;
		try {
			currentField = clas.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + name + " (" + clas.getName() + ")");
			return false;
		}

		currentField.setAccessible(true);

		try {
			currentField.set(instance, Integer.valueOf(value));
		} catch (IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + name + " (" + clas.getName() + ")");
			return false;
		}

		currentField.setAccessible(false);
		ConsoleUtil.info("Successfully set class " + name + " stack size to: " + value + " (" + clas.getName() + ")");
		return true;
	}

	public static boolean setItemField(String materialName, int stack) {
		if (VersionUtil.v1_20_R4.isServerVersionHigher()) {
			Object componentItem = getNmsItem(materialName);
			if (componentItem == null) {
				ConsoleUtil.warning("Failed to set the max item stack size of: " + materialName + ".");
				return false;
			}
			return setComponentField(componentItem, materialName, stack);
		}

		Field itemField;
		Object item;

		try {
			itemField = VersionUtil.v1_18_R1.isServerVersionHigher() ? findItemClassFromName(materialName)
					: ItemHandler.getInstance().getItemsClass().getDeclaredField(materialName.toUpperCase());
		} catch (SecurityException | NoSuchFieldException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + materialName + ".");
			return false;
		}

		try {
			item = itemField.get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + materialName + ".");
			return false;
		}

		if (VersionUtil.v1_20_R4.isServerVersionHigher()) {
			return setComponentField(item, materialName, stack);
		}

		return setClassField(ItemHandler.getInstance().getItemClass(), item,
				ItemHandler.getInstance().getServerVersion().getFieldName(), stack, materialName);
	}

	private static Object getNmsItem(String materialName) {
		Material material = Material.matchMaterial(materialName);
		if (material == null) {
			return null;
		}

		try {
			Class<?> craftMagicNumbers = Class.forName("org.bukkit.craftbukkit.util.CraftMagicNumbers");
			return craftMagicNumbers.getMethod("getItem", Material.class).invoke(null, material);
		} catch (ReflectiveOperationException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Field findItemClassFromName(String materialName) {
		for (Field field : ItemHandler.getInstance().getItemsClass().getFields()) {
			Object found;

			try {
				found = field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
				continue;
			}

			if (found == null)
				continue;

			String name;

			try {
				name = (String) ItemHandler.getInstance().getItemNameMethod().invoke(found);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| ClassCastException e) {
				e.printStackTrace();
				continue;
			}

			String[] split = name.split("\\.");

			name = split[split.length - 1];

			if (name.equals(materialName)) {
				return field;
			}
		}
		return null;
	}

	public static void updateAllItems(List<String> exemptMaterials, int stackSize) {
		if (VersionUtil.v1_20_R4.isServerVersionHigher()) {
			for (XMaterialUtil xMaterial : XMaterialUtil.VALUES) {
				Material material = xMaterial.parseMaterial();
				if (material == null || material == Material.AIR || !material.isItem()) {
					continue;
				}

				String name = material.name().toLowerCase(java.util.Locale.ROOT);
				if (exemptMaterials.contains(name)) {
					ConsoleUtil.info("Material: " + name + " is whitelisted. Skipping this entry.");
					continue;
				}

				int defaultSize = getDefaultStackValue(Material.class, material, "maxStack", name);
				ItemHandler.getInstance().getCachedMaterialSizes().put(xMaterial, defaultSize);
				setClassField(Material.class, material, "maxStack", stackSize, name);

				if (!InventoryStacks.getInstance().getConfig().getBoolean("only-stack-via-command")) {
					setItemField(name, stackSize);
				}
			}
			return;
		}

		for (Field field : ItemHandler.getInstance().getItemsClass().getFields()) {
			Object found;

			try {
				found = field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
				continue;
			}

			if (found == null)
				continue;

			String name;

			try {
				name = (String) ItemHandler.getInstance().getItemNameMethod().invoke(found);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| ClassCastException e) {
				e.printStackTrace();
				continue;
			}

			String[] split = name.split("\\.");

			name = split[split.length - 1];

			if (name.equalsIgnoreCase("air"))
				continue;

			if (exemptMaterials.contains(name)) {
				ConsoleUtil.info("Material: " + name + " is whitelisted. Skipping this entry.");
				continue;
			}

			Optional<XMaterialUtil> foundMaterial = XMaterialUtil.matchXMaterial(name);

			if (!foundMaterial.isPresent()) {
				ConsoleUtil
						.warning("Could not find a valid material with the name: " + name + ". Skipping this entry.");
				continue;
			}

			XMaterialUtil xMaterial = foundMaterial.get();
			Material material = xMaterial.parseMaterial();

			if (material == null || material == Material.AIR) {
				ConsoleUtil.warning(
						"Could not find a valid material with the name: " + name + ". Skipping this entry.");
				continue;
			}

			int defaultSize = getDefaultStackValue(Material.class, material, "maxStack", name);
			ItemHandler.getInstance().getCachedMaterialSizes().put(xMaterial, defaultSize);
			setClassField(Material.class, material, "maxStack", stackSize, name);

			if (!InventoryStacks.getInstance().getConfig().getBoolean("only-stack-via-command")) {
				setItemField(name, stackSize);
			}
		}
	}

	public static boolean setComponentField(Object item, String materialName, int stack) {
		try {
			Object components = ItemHandler.getInstance().getItemComponents(item);
			ItemHandler.getInstance().getComponentMapPutMethod().invoke(
					ItemHandler.getInstance().getComponentsMapField().get(components),
					new Object[] { ItemHandler.getInstance().getMaxStackSize(), Integer.valueOf(stack) });
		} catch (IllegalArgumentException | SecurityException | ReflectiveOperationException e) {
			e.printStackTrace();
			ConsoleUtil.warning("Failed to set the max item stack size of: " + materialName + ".");
			return false;
		}
		ConsoleUtil.info("Successfully set component " + materialName + " stack size to: " + stack);
		return true;
	}
}