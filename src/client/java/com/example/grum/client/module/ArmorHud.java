package com.example.grum.client.module;

import com.example.grum.client.menu.HudRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ArmorHud {
	public static final String ID = "armor_hud";

	private ArmorHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui || mc.screen != null) return;
		Player player = mc.player;

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
		int[] pos = HudRegistry.get(ID, sw / 2 - total / 2, sh - 60);
		int x = pos[0], y = pos[1];
		int startX = x;

		for (ItemStack s : stacks) {
			gui.renderItem(s, x, y);
			gui.renderItemDecorations(mc.font, s, x, y);
			x += slot + spacing;
		}
		HudRegistry.recordBounds(ID, startX, y, total, slot);
	}
}
