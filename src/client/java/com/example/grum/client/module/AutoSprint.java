package com.example.grum.client.module;

import net.minecraft.client.Minecraft;

public final class AutoSprint {
	private AutoSprint() {}

	public static void tick(Minecraft mc) {
		if (mc.player == null) return;
		if (mc.player.isUsingItem()) return;
		if (mc.player.getFoodData().getFoodLevel() <= 6) return;
		if (mc.player.zza > 0) {
			mc.player.setSprinting(true);
		}
	}
}
