package com.codingguru.inventorystacks.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.codingguru.inventorystacks.InventoryStacks;
import com.codingguru.inventorystacks.util.ConsoleUtil;
import com.codingguru.inventorystacks.util.ReflectionUtil;
import com.codingguru.inventorystacks.util.ServerTypeUtil;
import com.codingguru.inventorystacks.util.VersionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ItemHandler {

	private static final ItemHandler INSTANCE = new ItemHandler();

	private final long itemChangeDelay = InventoryStacks.getInstance().getConfig().getLong("item-change-delay", 2L);

	private Map<Material, Integer> cachedMaterials;

	private VersionUtil serverVersion;
	private ServerTypeUtil serverType;

	private Class<?> itemClass;
	private Class<?> itemsClass;
	private Class<?> dataComponentsClass;

	private Method getItemNameMethod;
	private Method componentMapPutMethod;
	private Method itemComponentsMethod;

	private Field itemsComponentsField;
	private Field componentsMapField;

	private Object maxStackSize;

	public ItemHandler() {
		cachedMaterials = Maps.newHashMap();
	}

	public void reloadInventoryStacks() {
		resetMaterialsToDefaultValues();
		setupLoadedMaterials();
	}

	public void resetMaterialsToDefaultValues() {
		for (Material mat : cachedMaterials.keySet()) {
			int defaultAmount = cachedMaterials.get(mat);
			String name = mat.name();
			ReflectionUtil.setClassField(Material.class, mat, "maxStack", defaultAmount, name.toLowerCase());
			ReflectionUtil.setItemField(name.toLowerCase(), defaultAmount);
		}
		cachedMaterials.clear();
	}

	public boolean setupServerVersion() {
		setupServerType();

		if (Bukkit.getBukkitVersion().startsWith("26.3")) {
			serverVersion = VersionUtil.v26_3_R1;
			return true;
		}

		if (Bukkit.getBukkitVersion().startsWith("26.2")) {
			serverVersion = VersionUtil.v26_2_R1;
			return true;
		}

		ConsoleUtil.warning("Unsupported server version: " + Bukkit.getBukkitVersion()
				+ ". This build only supports 26.2 and 26.3.");
		return false;
	}

	public void setupServerType() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			serverType = ServerTypeUtil.FOLIA;
			return;
		} catch (ClassNotFoundException e) {
		}

		try {
			Class.forName("io.papermc.paper.ServerBuildInfo");
			serverType = ServerTypeUtil.PAPER;
			return;
		} catch (ClassNotFoundException e) {
		}

		serverType = ServerTypeUtil.SPIGOT;

	}

	public void setupReflectionClasses() {
		VersionUtil serverVersion = ItemHandler.getInstance().getServerVersion();

		try {
			itemClass = Class.forName(serverVersion.getItemClass());
			itemsClass = Class.forName(serverVersion.getItemsClass());
			getItemNameMethod = itemClass.getMethod(serverVersion.getItemNameMethod());
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		try {
			dataComponentsClass = Class.forName("net.minecraft.core.component.DataComponents");
			maxStackSize = dataComponentsClass.getField(serverType.getMaxStackField()).get(null);

			Object sampleItem = itemsClass.getFields()[0].get(null);
			Object itemComponents;

			try {
				itemsComponentsField = itemClass.getDeclaredField(serverType.getItemComponentsField());
				itemsComponentsField.setAccessible(true);
				itemComponents = itemsComponentsField.get(sampleItem);
			} catch (NoSuchFieldException e) {
				itemComponentsMethod = itemClass.getMethod(serverType.getItemComponentsField());
				itemComponents = itemComponentsMethod.invoke(sampleItem);
			}

			componentsMapField = itemComponents.getClass().getDeclaredField(serverType.getComponentsMapField());
			componentsMapField.setAccessible(true);
			componentMapPutMethod = componentsMapField.getType().getMethod("put",
					new Class[] { Object.class, Object.class });
		} catch (SecurityException | IllegalArgumentException | ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	public void setupLoadedMaterials() {
		Map<String, Integer> materialListRegex = getFoundMaterialList(Lists.newArrayList(
				InventoryStacks.getInstance().getConfig().getConfigurationSection("items").getKeys(true)));

		for (String materialName : materialListRegex.keySet()) {
			Material material = Material.matchMaterial(materialName);

			if (material == null || material == Material.AIR) {
				ConsoleUtil.warning(
						"Could not find a valid material with the name: " + materialName + ". Skipping this entry.");
				continue;
			}

			int stackSize = materialListRegex.get(materialName);

			if (ReflectionUtil.isUnstackableItem(material, stackSize)) {
				ConsoleUtil.warning("Skipping " + materialName
						+ ": damageable items cannot be stacked on this server version.");
				continue;
			}

			String name = material.name();
			int defaultSize = ReflectionUtil.getDefaultStackValue(Material.class, material, "maxStack",
					name.toLowerCase());
			getCachedMaterialSizes().put(material, defaultSize);
			ReflectionUtil.setClassField(Material.class, material, "maxStack", stackSize, name.toLowerCase());

			if (!InventoryStacks.getInstance().getConfig().getBoolean("only-stack-via-command")) {
				ReflectionUtil.setItemField(name.toLowerCase(), stackSize);
			}
		}

		if (InventoryStacks.getInstance().getConfig().getBoolean("max-stack-for-all-items.enabled")) {
			int stackSize = InventoryStacks.getInstance().getConfig().getInt("max-stack-for-all-items.amount");

			if (stackSize > getServerVersion().getAbsoluteMaxStackSize()) {
				stackSize = getServerVersion().getAbsoluteMaxStackSize();
				ConsoleUtil.warning("Could not set stack size over " + getServerVersion().getAbsoluteMaxStackSize()
						+ ". Defaulting to max value for ALL items.");
			}

			List<String> configExemptList = InventoryStacks.getInstance().getConfig()
					.isSet("max-stack-for-all-items.whitelist")
							? InventoryStacks.getInstance().getConfig()
									.getStringList("max-stack-for-all-items.whitelist")
							: InventoryStacks.getInstance().getConfig()
									.getStringList("max-stack-for-all-items.blacklist");

			List<String> exemptMaterials = configExemptList.stream().map(String::toLowerCase)
					.collect(Collectors.toList());

			materialListRegex.keySet().forEach(name -> exemptMaterials.add(name.toLowerCase()));

			ReflectionUtil.updateAllItems(exemptMaterials, stackSize);
		}
	}

	public Map<String, Integer> getFoundMaterialList(List<String> materialNames) {
		Map<String, Integer> foundTypes = Maps.newHashMap();

		for (String regexPattern : materialNames) {
			Pattern pattern = Pattern.compile(regexPattern);

			for (Material material : Material.values()) {
				String name = material.name();
				Matcher m = pattern.matcher(name);

				while (m.find()) {
					if (m.group().trim().length() > 0) {
						foundTypes.put(m.group(), getStackSize(regexPattern));
					}
				}
			}
		}

		return foundTypes;
	}

	public int getStackSize(String itemPath) {
		int stackSize = InventoryStacks.getInstance().getConfig().getInt("items." + itemPath);

		if (stackSize > getServerVersion().getAbsoluteMaxStackSize()) {
			stackSize = getServerVersion().getAbsoluteMaxStackSize();
			ConsoleUtil.warning("Could not set " + itemPath + " stack size over "
					+ getServerVersion().getAbsoluteMaxStackSize() + ". Defaulting to max value.");
		}

		return stackSize;
	}

	public Map<Material, Integer> getCachedMaterialSizes() {
		return cachedMaterials;
	}

	public Object getMaxStackSize() {
		return maxStackSize;
	}

	public Field getComponentsMapField() {
		return componentsMapField;
	}

	public Field getItemComponentsField() {
		return itemsComponentsField;
	}

	public Object getItemComponents(Object item) throws ReflectiveOperationException {
		return itemComponentsMethod != null ? itemComponentsMethod.invoke(item) : itemsComponentsField.get(item);
	}

	public Method getComponentMapPutMethod() {
		return componentMapPutMethod;
	}

	public Method getItemNameMethod() {
		return getItemNameMethod;
	}

	public Class<?> getItemClass() {
		return itemClass;
	}

	public Class<?> getItemsClass() {
		return itemsClass;
	}

	public VersionUtil getServerVersion() {
		return serverVersion;
	}

	public ServerTypeUtil getServerType() {
		return serverType;
	}

	public long getItemChangeDelay() {
		return itemChangeDelay;
	}

	public static ItemHandler getInstance() {
		return INSTANCE;
	}
}