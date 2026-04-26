package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Мини-предпросмотр инвентаря (3 ряда по 9) в правом нижнем углу. */
public final class InventoryHud {
	public static final String ID = "inventory_hud";
	private InventoryHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui || mc.screen != null) return;
		Inventory inv = mc.player.getInventory();

		int slot = 18, gap = 1;
		int gridW = 9 * slot + 8 * gap;
		int gridH = 3 * slot + 2 * gap;
		int padding = 4;
		int boxW = gridW + padding * 2;
		int boxH = gridH + padding * 2;
		int[] pos = com.example.grum.client.menu.HudRegistry.get(ID, gui.guiWidth() - boxW - 6, gui.guiHeight() - boxH - 60);
		int x = pos[0], y = pos[1];

		gui.fill(x, y, x + boxW, y + boxH, 0xCC151821);
		gui.fill(x, y, x + boxW, y + 1, 0xFF2A2E3A);
		gui.fill(x, y + boxH - 1, x + boxW, y + boxH, 0xFF2A2E3A);

		// инвентарь — слоты 9..35 (3 ряда основной части, без хотбара)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int slotIdx = 9 + row * 9 + col;
				ItemStack s = inv.getItem(slotIdx);
				if (s.isEmpty()) continue;
				int sx = x + padding + col * (slot + gap);
				int sy = y + padding + row * (slot + gap);
				gui.renderItem(s, sx, sy);
				gui.renderItemDecorations(mc.font, s, sx, sy);
			}
		}
		com.example.grum.client.menu.HudRegistry.recordBounds(ID, x, y, boxW, boxH);
	}
}
