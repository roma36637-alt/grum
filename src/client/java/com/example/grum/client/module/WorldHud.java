package com.example.grum.client.module;

import com.example.grum.client.menu.HudRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class WorldHud {
	public static final String ID = "world_info";

	private WorldHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.options.hideGui) return;
		Player p = mc.player;

		String[] lines = {
				String.format("XYZ: %.1f / %.1f / %.1f", p.getX(), p.getY(), p.getZ()),
				"Dim: " + mc.level.dimension().location().toString(),
				"Biome: " + mc.level.getBiome(p.blockPosition()).unwrapKey().map(k -> k.location().getPath()).orElse("?"),
				"FPS: " + mc.getFps()
		};

		int w = 0;
		for (String s : lines) w = Math.max(w, mc.font.width(s));
		int padding = 6;
		int boxW = w + padding * 2;
		int lineH = 10;
		int boxH = lines.length * lineH + padding * 2;

		int[] pos = HudRegistry.get(ID, gui.guiWidth() - boxW - 6, 6);
		int x = pos[0], y = pos[1];

		gui.fill(x, y, x + boxW, y + boxH, 0xCC151821);
		gui.fill(x, y, x + boxW, y + 1, 0xFF2A2E3A);
		gui.fill(x, y + boxH - 1, x + boxW, y + boxH, 0xFF2A2E3A);
		for (int i = 0; i < lines.length; i++) {
			gui.drawString(mc.font, lines[i], x + padding, y + padding + i * lineH, 0xFFE0E0E0, false);
		}
		HudRegistry.recordBounds(ID, x, y, boxW, boxH);
	}
}
