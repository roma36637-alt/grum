package com.example.grum.client.module;

import net.minecraft.client.Minecraft;

/** Слушает чат: если кто-то отправил TPA — выполняет /tpaccept. */
public final class AutoAccept {
	private AutoAccept() {}

	public static void onChat(String message) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.getConnection() == null) return;
		String low = message.toLowerCase();
		if (low.contains("/tpaccept") || (low.contains("tpa") && (low.contains("отправил") || low.contains("requested") || low.contains("хочет")))) {
			mc.player.connection.sendCommand("tpaccept");
		}
	}
}
