package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/** Автоматически выполняет команду входа на режим (по конфигу autoJoinCommand). */
public final class AutoJoin {
	private static boolean sentForCurrentSession = false;
	private static String lastWorld = "";

	private AutoJoin() {}

	public static void tick(Minecraft mc) {
		if (mc.player == null || mc.level == null) {
			sentForCurrentSession = false;
			lastWorld = "";
			return;
		}
		String world = mc.level.dimension().location().toString();
		if (!world.equals(lastWorld)) {
			lastWorld = world;
			sentForCurrentSession = false;
		}
		if (sentForCurrentSession) return;

		String cmd = GrumConfig.get().autoJoinCommand;
		if (cmd == null || cmd.isBlank()) return;
		ClientPacketListener cpl = mc.player.connection;
		if (cpl == null) return;
		if (cmd.startsWith("/")) cmd = cmd.substring(1);
		cpl.sendCommand(cmd);
		sentForCurrentSession = true;
	}
}
