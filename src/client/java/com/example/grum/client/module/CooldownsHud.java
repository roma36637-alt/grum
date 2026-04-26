package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Показывает иконки + проценты предметов с активным кулдауном (жемчуг, хорус и т.п.). */
public final class CooldownsHud {
	private static final ItemStack[] TRACKED = {
			new ItemStack(Items.ENDER_PEARL),
			new ItemStack(Items.CHORUS_FRUIT),
			new ItemStack(Items.SHIELD),
			new ItemStack(Items.GOAT_HORN)
	};

	private CooldownsHud() {}

	public static void render(GuiGraphics gui) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		Player p = mc.player;
		ItemCooldowns cd = p.getCooldowns();

		List<ItemStack> active = new ArrayList<>();
		List<Float> percents = new ArrayList<>();
		for (ItemStack s : TRACKED) {
			float pct = cd.getCooldownPercent(s, 0);
			if (pct > 0) {
				active.add(s);
				percents.add(pct);
			}
		}
		if (active.isEmpty()) return;

		int slot = 18;
		int spacing = 2;
		int total = active.size() * slot + (active.size() - 1) * spacing;
		int x = gui.guiWidth() / 2 - total / 2;
		int y = 6;

		for (int i = 0; i < active.size(); i++) {
			ItemStack s = active.get(i);
			gui.renderItem(s, x, y);
			float pct = percents.get(i);
			String text = String.format("%.1f", pct * 5.0f) + "s";
			gui.drawString(mc.font, text, x + slot / 2 - mc.font.width(text) / 2, y + slot + 2, 0xFFFFFFFF, true);
			x += slot + spacing;
		}
	}
}
