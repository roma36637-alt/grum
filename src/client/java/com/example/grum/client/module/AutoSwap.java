package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Если в оффхенде нет тотема, но он есть в инвентаре — кладёт тотем в оффхенд. */
public final class AutoSwap {
	private static long lastSwap = 0;

	private AutoSwap() {}

	public static void tick(Minecraft mc) {
		if (mc.player == null || mc.gameMode == null) return;
		long now = System.currentTimeMillis();
		if (now - lastSwap < 500) return;

		Inventory inv = mc.player.getInventory();
		ItemStack offhand = mc.player.getOffhandItem();
		if (offhand.is(Items.TOTEM_OF_UNDYING)) return;

		int totemSlot = -1;
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (inv.getItem(i).is(Items.TOTEM_OF_UNDYING)) { totemSlot = i; break; }
		}
		if (totemSlot < 0) return;

		// pickup item from totemSlot, place in offhand (slot 45)
		int containerSlot = totemSlot < 9 ? 36 + totemSlot : totemSlot;
		mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, containerSlot, 0,
				net.minecraft.world.inventory.ClickType.PICKUP, mc.player);
		mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, 45, 0,
				net.minecraft.world.inventory.ClickType.PICKUP, mc.player);
		mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, containerSlot, 0,
				net.minecraft.world.inventory.ClickType.PICKUP, mc.player);
		lastSwap = now;
	}
}
