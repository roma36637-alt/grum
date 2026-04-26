package com.example.grum.client.module;

import com.example.grum.client.menu.HudRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;

public final class KeybindsHud {
	public static final String ID = "keybinds";

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
		int rowH = 11;
		int maxW = 0;
		for (String k : keys) maxW = Math.max(maxW, mc.font.width(k));
		int boxW = maxW + padding * 2;
		int boxH = keys.length * rowH;

		int[] pos = HudRegistry.get(ID, 6, gui.guiHeight() - boxH - 80);
		int boxX = pos[0], boxY = pos[1];

		for (int i = 0; i < keys.length; i++) {
			String k = keys[i];
			int color = active[i] ? 0xFFFFFFFF : 0xFF606060;
			int bg = active[i] ? 0xCC1F2530 : 0x55151821;
			gui.fill(boxX, boxY + i * rowH, boxX + boxW, boxY + i * rowH + rowH - 1, bg);
			gui.drawString(mc.font, k, boxX + padding, boxY + i * rowH + 2, color, false);
		}
		HudRegistry.recordBounds(ID, boxX, boxY, boxW, boxH);
	}
}
