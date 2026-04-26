package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.ArrayDeque;
import java.util.Deque;

/** Плашка-уведомление в верхней части экрана (стиль iPhone Dynamic Island). */
public final class DynamicIsland {
	public static final String ID = "dynamic_island";
	private static final ResourceLocation LOGO = ResourceLocation.fromNamespaceAndPath("grum", "icon.png");
	private static final long DURATION_MS = 3500;
	private static final Deque<Notification> queue = new ArrayDeque<>();

	private DynamicIsland() {}

	public static void notify(String text) {
		queue.addLast(new Notification(text, System.currentTimeMillis()));
	}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.screen != null) return;

		long now = System.currentTimeMillis();
		while (!queue.isEmpty() && now - queue.peekFirst().created > DURATION_MS) queue.removeFirst();

		String text;
		if (queue.isEmpty()) {
			// дефолтный режим: "ватермарка"
			text = "grum  •  " + mc.getFps() + " fps";
		} else {
			text = queue.peekFirst().text;
		}

		Font font = mc.font;
		int padding = 10;
		int iconSize = 14;
		int w = font.width(text) + padding * 2 + iconSize + 6;
		int h = 22;
		int[] pos = com.example.grum.client.menu.HudRegistry.get(ID, gui.guiWidth() / 2 - w / 2, 6);
		int x = pos[0], y = pos[1];

		// тёмная плашка с скруглением имитируем доп. рамкой
		gui.fill(x + 1, y, x + w - 1, y + h, 0xE8101015);
		gui.fill(x, y + 1, x + w, y + h - 1, 0xE8101015);
		gui.fill(x, y, x + 3, y + h, GrumConfig.get().accentColor);

		gui.blit(RenderPipelines.GUI_TEXTURED, LOGO, x + padding, y + (h - iconSize) / 2, 0f, 0f,
				iconSize, iconSize, iconSize, iconSize);
		gui.drawString(font, text, x + padding + iconSize + 6, y + 7, 0xFFFFFFFF, false);
		com.example.grum.client.menu.HudRegistry.recordBounds(ID, x, y, w, h);
	}

	private static class Notification {
		final String text; final long created;
		Notification(String t, long c) { text = t; created = c; }
	}
}
