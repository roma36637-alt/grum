package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;

/** Периодически отправляет команду /near. */
public final class AutoNear {
	private static long lastSent = 0;

	private AutoNear() {}

	public static void tick(Minecraft mc) {
		if (mc.player == null) return;
		long now = System.currentTimeMillis();
		long interval = GrumConfig.get().autoNearIntervalMs;
		if (now - lastSent < interval) return;
		mc.player.connection.sendCommand("near");
		lastSent = now;
	}
}
