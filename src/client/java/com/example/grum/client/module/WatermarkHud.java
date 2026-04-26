package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class WatermarkHud {
	private WatermarkHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui) return;
		int fps = mc.getFps();
		String text = "grum  " + fps + " fps";
		int padding = 6;
		int w = mc.font.width(text) + padding * 2;
		int h = 16;
		int x = 6, y = 6;
		gui.fill(x, y, x + w, y + h, 0xCC151821);
		gui.fill(x, y, x + 3, y + h, GrumConfig.get().accentColor);
		gui.drawString(mc.font, text, x + padding + 4, y + 4, 0xFFFFFFFF, false);
	}
}
