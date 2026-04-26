package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class DeathCoords {
	private static boolean wasAlive = false;

	private DeathCoords() {}

	public static void tick(Minecraft mc) {
		Player p = mc.player;
		if (p == null) return;
		if (!p.isAlive() && wasAlive) {
			String msg = String.format("[grum] умер на: %.0f %.0f %.0f (%s)",
					p.getX(), p.getY(), p.getZ(),
					mc.level == null ? "?" : mc.level.dimension().location().toString());
			mc.gui.getChat().addMessage(Component.literal(msg));
			wasAlive = false;
		} else if (p.isAlive()) {
			wasAlive = true;
		}
	}
}
