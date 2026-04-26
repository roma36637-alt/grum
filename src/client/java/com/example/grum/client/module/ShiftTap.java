package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** Кратко "отжимает" шифт перед ударом, чтобы получить нормальный нокдаун/крит. */
public final class ShiftTap {
	private static int suppressTicks = 0;

	private ShiftTap() {}

	public static void onAttack(Entity target) {
		suppressTicks = 2;
	}

	public static void tick(Minecraft mc) {
		if (suppressTicks > 0) {
			mc.options.keyShift.setDown(false);
			suppressTicks--;
		}
	}
}
