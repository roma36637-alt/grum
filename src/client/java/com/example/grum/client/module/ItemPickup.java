package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.Set;

/** Уведомляет в чат когда в инвентаре появляются ценные предметы. */
public final class ItemPickup {
	private static final Set<net.minecraft.world.item.Item> WATCHED = Set.of(
			Items.TOTEM_OF_UNDYING, Items.ENCHANTED_GOLDEN_APPLE,
			Items.NETHERITE_INGOT, Items.NETHERITE_SCRAP,
			Items.NETHER_STAR, Items.ELYTRA, Items.HEART_OF_THE_SEA, Items.DRAGON_EGG
	);
	private static final Set<String> announced = new HashSet<>();

	private ItemPickup() {}

	public static void tick(Minecraft mc) {
		if (mc.player == null) {
			announced.clear();
			return;
		}
		Set<String> currently = new HashSet<>();
		for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
			ItemStack s = mc.player.getInventory().getItem(i);
			if (s.isEmpty()) continue;
			if (WATCHED.contains(s.getItem())) {
				String key = s.getItem().getDescriptionId() + "@" + i;
				currently.add(key);
				if (!announced.contains(key)) {
					mc.gui.getChat().addMessage(Component.literal("[grum] подобрано: " + s.getHoverName().getString()));
				}
			}
		}
		announced.clear();
		announced.addAll(currently);
	}
}
