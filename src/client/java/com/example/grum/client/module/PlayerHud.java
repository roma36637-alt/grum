package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import com.example.grum.client.menu.HudRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class PlayerHud {
	public static final String ID = "player_info";

	private PlayerHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		Player p = mc.player;

		String hp = String.format("HP %.1f / %.1f", p.getHealth(), p.getMaxHealth());
		String hunger = "Food " + p.getFoodData().getFoodLevel() + " / 20";
		String xp = "XP " + p.experienceLevel;
		String[] lines = { hp, hunger, xp };

		int w = 0;
		for (String s : lines) w = Math.max(w, mc.font.width(s));
		int padding = 6;
		int boxW = w + padding * 2 + 4;
		int lineH = 10;
		int boxH = lines.length * lineH + padding * 2;

		int[] pos = HudRegistry.get(ID, gui.guiWidth() - boxW - 6, gui.guiHeight() - boxH - 60);
		int x = pos[0], y = pos[1];

		gui.fill(x, y, x + boxW, y + boxH, 0xCC151821);
		gui.fill(x, y, x + 3, y + boxH, GrumConfig.get().accentColor);
		for (int i = 0; i < lines.length; i++) {
			gui.drawString(mc.font, lines[i], x + padding + 4, y + padding + i * lineH, 0xFFE0E0E0, false);
		}
		HudRegistry.recordBounds(ID, x, y, boxW, boxH);
	}
}
