package com.example.grum.client.menu;

import com.example.grum.client.GrumConfig;
import com.example.grum.client.module.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;

/** Экран-редактор: рисует все включённые HUD'ы и позволяет их перетаскивать мышкой. */
public class HudEditorScreen extends Screen {
	private String dragging = null;
	private int dragOffsetX, dragOffsetY;

	public HudEditorScreen() {
		super(Component.literal("grum hud editor"));
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		// затемнение фона
		gui.fill(0, 0, this.width, this.height, 0x80000000);

		// рисуем сами HUD'ы (они вызовут recordBounds)
		GrumConfig cfg = GrumConfig.get();
		if (cfg.armorHud) ArmorHud.render(gui);
		if (cfg.targetHud) TargetHud.render(gui);
		if (cfg.potionsHud) PotionsHud.render(gui);
		if (cfg.watermarkHud) WatermarkHud.render(gui);
		if (cfg.worldHud) WorldHud.render(gui);
		if (cfg.playerHud) PlayerHud.render(gui);
		if (cfg.keybindsHud) KeybindsHud.render(gui);
		if (cfg.cooldownsHud) CooldownsHud.render(gui);
		if (cfg.dynamicIsland) DynamicIsland.render(gui);
		if (cfg.inventoryHud) InventoryHud.render(gui);
		if (cfg.itemHighlighter) ItemHighlighter.render(gui);

		// рамки вокруг каждого HUD
		for (var entry : HudRegistry.getBounds().entrySet()) {
			int[] b = entry.getValue();
			boolean hovered = mouseX >= b[0] && mouseX < b[0] + b[2] && mouseY >= b[1] && mouseY < b[1] + b[3];
			int color = entry.getKey().equals(dragging) ? cfg.accentColor : (hovered ? 0xFF80B0FF : 0x80FFFFFF);
			drawRect(gui, b[0] - 1, b[1] - 1, b[0] + b[2] + 1, b[1] + b[3] + 1, color);
		}

		// подсказка сверху
		String hint = "Перетаскивай HUD-элементы мышкой. ESC чтобы выйти и сохранить.";
		gui.fill(0, 0, this.width, 22, 0xC0151821);
		gui.drawCenteredString(this.font, hint, this.width / 2, 7, 0xFFFFFFFF);

		super.render(gui, mouseX, mouseY, partialTick);
	}

	private void drawRect(GuiGraphics gui, int x1, int y1, int x2, int y2, int color) {
		gui.fill(x1, y1, x2, y1 + 1, color);
		gui.fill(x1, y2 - 1, x2, y2, color);
		gui.fill(x1, y1, x1 + 1, y2, color);
		gui.fill(x2 - 1, y1, x2, y2, color);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button != 0) return super.mouseClicked(mx, my, button);
		int x = (int) mx, y = (int) my;
		// найдём верхний HUD под курсором
		String found = null;
		for (Map.Entry<String, int[]> entry : HudRegistry.getBounds().entrySet()) {
			int[] b = entry.getValue();
			if (x >= b[0] && x < b[0] + b[2] && y >= b[1] && y < b[1] + b[3]) {
				found = entry.getKey();
			}
		}
		if (found != null) {
			dragging = found;
			int[] b = HudRegistry.getBounds().get(found);
			dragOffsetX = x - b[0];
			dragOffsetY = y - b[1];
			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
		if (dragging != null) {
			int newX = (int) mx - dragOffsetX;
			int newY = (int) my - dragOffsetY;
			// границы экрана
			int[] b = HudRegistry.getBounds().get(dragging);
			if (b != null) {
				newX = Math.max(0, Math.min(this.width - b[2], newX));
				newY = Math.max(0, Math.min(this.height - b[3], newY));
			}
			HudRegistry.setPos(dragging, newX, newY);
			return true;
		}
		return super.mouseDragged(mx, my, button, dx, dy);
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		if (dragging != null) {
			dragging = null;
			GrumConfig.get().save();
			return true;
		}
		return super.mouseReleased(mx, my, button);
	}

	@Override
	public boolean isPauseScreen() { return false; }

	@Override
	public void onClose() {
		GrumConfig.get().save();
		if (this.minecraft != null) this.minecraft.setScreen(null);
	}
}
