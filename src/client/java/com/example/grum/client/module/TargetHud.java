package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Карточка цели слева снизу: имя + HP-полоска. Показывает того, на кого навёл прицел. */
public final class TargetHud {
	private static LivingEntity lastTarget;
	private static long lastSeen;

	private TargetHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui || mc.screen != null) return;

		LivingEntity target = currentTarget(mc);
		long now = System.currentTimeMillis();
		if (target != null) {
			lastTarget = target;
			lastSeen = now;
		}
		if (lastTarget == null || !lastTarget.isAlive()) return;
		if (now - lastSeen > 2500) return;

		Font font = mc.font;
		String name = lastTarget.getDisplayName().getString();
		float hp = Math.max(0, lastTarget.getHealth());
		float max = Math.max(1, lastTarget.getMaxHealth());
		String hpStr = String.format("%.1f / %.1f", hp, max);

		int padding = 8;
		int width = Math.max(120, Math.max(font.width(name), font.width(hpStr)) + padding * 2);
		int height = 44;
		int x = 10;
		int y = gui.guiHeight() - height - 50;

		// фон
		gui.fill(x, y, x + width, y + height, 0xCC151821);
		gui.fill(x, y, x + width, y + 1, 0xFF2A2E3A);
		gui.fill(x, y + height - 1, x + width, y + height, 0xFF2A2E3A);
		gui.fill(x, y, x + 1, y + height, 0xFF2A2E3A);
		gui.fill(x + width - 1, y, x + width, y + height, 0xFF2A2E3A);

		// имя
		gui.drawString(font, name, x + padding, y + 6, 0xFFFFFFFF, false);

		// hp bar
		int barX = x + padding;
		int barY = y + 20;
		int barW = width - padding * 2;
		int barH = 6;
		gui.fill(barX, barY, barX + barW, barY + barH, 0xFF2A2E3A);
		int filled = Math.round(barW * (hp / max));
		int color = GrumConfig.get().accentColor;
		gui.fill(barX, barY, barX + filled, barY + barH, color);

		// hp text
		gui.drawString(font, hpStr, x + padding, y + 30, 0xFFC0C0C0, false);
	}

	private static LivingEntity currentTarget(Minecraft mc) {
		if (mc.hitResult instanceof EntityHitResult ehr) {
			Entity e = ehr.getEntity();
			if (e instanceof LivingEntity le) return le;
		}
		return null;
	}
}
