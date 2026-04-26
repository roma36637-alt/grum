package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Рисует броню (4 предмета) и оффхенд над хотбаром, с прочностью. */
public final class ArmorHud {
	private ArmorHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		if (mc.screen != null) return;
		Player player = mc.player;

		// собираем непустые слоты (порядок: шлем, нагрудник, поножи, ботинки)
		List<ItemStack> stacks = new ArrayList<>();
		EquipmentSlot[] order = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
		for (EquipmentSlot slot : order) {
			ItemStack s = player.getItemBySlot(slot);
			if (!s.isEmpty()) stacks.add(s);
		}
		ItemStack offhand = player.getOffhandItem();
		if (!offhand.isEmpty()) stacks.add(offhand);

		if (stacks.isEmpty()) return;

		int slot = 18;
		int spacing = 2;
		int total = stacks.size() * slot + (stacks.size() - 1) * spacing;
		int sw = gui.guiWidth();
		int sh = gui.guiHeight();
		int x = sw / 2 - total / 2;
		int y = sh - 60; // над хотбаром

		for (ItemStack s : stacks) {
			gui.renderItem(s, x, y);
			gui.renderItemDecorations(mc.font, s, x, y);
			x += slot + spacing;
		}
	}
}
