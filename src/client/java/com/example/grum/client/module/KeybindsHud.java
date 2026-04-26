package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;

public final class KeybindsHud {
	private KeybindsHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		Options o = mc.options;

		String[] keys = { "W", "A", "S", "D", "SPACE", "SHIFT" };
		boolean[] active = {
				o.keyUp.isDown(), o.keyLeft.isDown(), o.keyDown.isDown(), o.keyRight.isDown(),
				o.keyJump.isDown(), o.keyShift.isDown()
		};

		int padding = 4;
		int boxX = 6;
		int boxY = gui.guiHeight() - 80;
		int rowH = 11;

		for (int i = 0; i < keys.length; i++) {
			String k = keys[i];
			int w = mc.font.width(k) + padding * 2;
			int color = active[i] ? 0xFFFFFFFF : 0xFF606060;
			int bg = active[i] ? 0xCC1F2530 : 0x55151821;
			gui.fill(boxX, boxY + i * rowH, boxX + w, boxY + i * rowH + rowH - 1, bg);
			gui.drawString(mc.font, k, boxX + padding, boxY + i * rowH + 2, color, false);
		}
	}
}
