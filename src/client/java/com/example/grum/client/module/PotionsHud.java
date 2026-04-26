package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

import java.util.Collection;

/** Список активных эффектов в правом верхнем углу. */
public final class PotionsHud {
	private PotionsHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui || mc.screen != null) return;

		Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
		if (effects.isEmpty()) return;

		Font font = mc.font;
		int rowH = 22;
		int width = 120;
		int x = gui.guiWidth() - width - 8;
		int y = 8;

		for (MobEffectInstance inst : effects) {
			MobEffect effect = inst.getEffect().value();
			String name = inst.getEffect().value().getDisplayName().getString();
			if (inst.getAmplifier() > 0) name += " " + roman(inst.getAmplifier() + 1);
			String time = MobEffectUtil.formatDuration(inst, 1.0f, mc.level == null ? 20 : mc.level.tickRateManager().tickrate()).getString();

			// фон
			gui.fill(x, y, x + width, y + rowH, 0xCC151821);
			gui.fill(x, y, x + width, y + 1, 0xFF2A2E3A);
			gui.fill(x, y + rowH - 1, x + width, y + rowH, 0xFF2A2E3A);

			// текст
			int textColor = effect.getColor() | 0xFF000000;
			gui.drawString(font, name, x + 6, y + 3, textColor, false);
			gui.drawString(font, time, x + 6, y + 12, 0xFFB0B0B0, false);

			y += rowH + 2;
		}
	}

	private static String roman(int n) {
		switch (n) {
			case 1: return "I";
			case 2: return "II";
			case 3: return "III";
			case 4: return "IV";
			case 5: return "V";
			default: return String.valueOf(n);
		}
	}
}
