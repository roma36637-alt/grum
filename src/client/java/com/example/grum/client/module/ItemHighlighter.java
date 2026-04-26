package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

/** Подсвечивает в хотбаре полезные предметы (тотем, золотые яблоки, жемчуг и т.п.). */
public final class ItemHighlighter {
	private static final Set<net.minecraft.world.item.Item> HIGHLIGHTED = Set.of(
			Items.TOTEM_OF_UNDYING,
			Items.ENCHANTED_GOLDEN_APPLE,
			Items.GOLDEN_APPLE,
			Items.ENDER_PEARL,
			Items.CHORUS_FRUIT
	);

	private ItemHighlighter() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;

		int sw = gui.guiWidth();
		int hotbarX = sw / 2 - 91;
		int hotbarY = gui.guiHeight() - 22;
		int color = (GrumConfig.get().accentColor & 0x00FFFFFF) | 0x80000000;

		for (int i = 0; i < 9; i++) {
			ItemStack s = mc.player.getInventory().getItem(i);
			if (HIGHLIGHTED.contains(s.getItem())) {
				int x = hotbarX + i * 20 + 2;
				int y = hotbarY + 2;
				gui.fill(x, y, x + 16, y + 16, color);
			}
		}
	}
}
