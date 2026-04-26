package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/** Всплывающие уведомления при получении нового статус-эффекта. */
public final class EffectNotify {
	private static final long DURATION_MS = 2500;
	private static final Deque<Notification> queue = new ArrayDeque<>();
	private static final Set<Holder<MobEffect>> known = new HashSet<>();
	private static boolean primed = false;

	private EffectNotify() {}

	public static void tick(Minecraft mc) {
		if (mc.player == null) {
			known.clear();
			primed = false;
			return;
		}
		Set<Holder<MobEffect>> current = new HashSet<>();
		for (MobEffectInstance inst : mc.player.getActiveEffects()) {
			current.add(inst.getEffect());
		}
		if (primed) {
			for (Holder<MobEffect> h : current) {
				if (!known.contains(h)) {
					String name = h.value().getDisplayName().getString();
					int color = h.value().getColor() | 0xFF000000;
					queue.addLast(new Notification(name, color, System.currentTimeMillis()));
				}
			}
		}
		known.clear();
		known.addAll(current);
		primed = true;

		// чистим устаревшие
		long now = System.currentTimeMillis();
		while (!queue.isEmpty() && now - queue.peekFirst().created > DURATION_MS) {
			queue.removeFirst();
		}
	}

	public static void render(GuiGraphics gui) {
		if (queue.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.screen != null) return;

		Font font = mc.font;
		int width = 160;
		int height = 22;
		int x = gui.guiWidth() / 2 - width / 2;
		int y = 30;
		long now = System.currentTimeMillis();

		for (Notification n : queue) {
			float t = (now - n.created) / (float) DURATION_MS;
			int alpha = (int) (255 * Math.min(1.0f, Math.min(t * 6f, (1.0f - t) * 6f)));
			if (alpha < 5) { y += height + 4; continue; }
			int aShift = (alpha & 0xFF) << 24;

			gui.fill(x, y, x + width, y + height, 0x00151821 | (Math.min(0xCC, alpha) << 24));
			gui.fill(x, y, x + 3, y + height, (n.color & 0x00FFFFFF) | aShift);

			int textCol = (0x00FFFFFF) | aShift;
			gui.drawString(font, "+ " + n.name, x + 8, y + (height - 8) / 2, textCol, false);
			y += height + 4;
		}
	}

	private static class Notification {
		final String name;
		final int color;
		final long created;

		Notification(String name, int color, long created) {
			this.name = name;
			this.color = color;
			this.created = created;
		}
	}
}
