package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public final class WatermarkHud {
	private static final ResourceLocation LOGO = ResourceLocation.fromNamespaceAndPath("grum", "icon.png");

	private WatermarkHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui) return;
		int fps = mc.getFps();
		String text = "grum  " + fps + " fps";
		int padding = 6;
		int iconSize = 14;
		int w = mc.font.width(text) + padding * 2 + iconSize + 4;
		int h = 18;
		int x = 6, y = 6;
		gui.fill(x, y, x + w, y + h, 0xCC151821);
		gui.fill(x, y, x + 3, y + h, GrumConfig.get().accentColor);

		// логотип
		gui.blit(RenderPipelines.GUI_TEXTURED, LOGO, x + padding, y + (h - iconSize) / 2, 0f, 0f,
				iconSize, iconSize, iconSize, iconSize);

		gui.drawString(mc.font, text, x + padding + iconSize + 4, y + 5, 0xFFFFFFFF, false);
	}
}
