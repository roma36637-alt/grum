package com.example.grum.client.menu;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class GrumMenuScreen extends Screen {
	private static String activeTab = "HUD";

	public GrumMenuScreen() {
		super(Component.literal("grum"));
	}

	@Override
	protected void init() {
		GrumConfig cfg = GrumConfig.get();

		int panelW = 520;
		int panelH = 320;
		int x0 = (this.width - panelW) / 2;
		int y0 = (this.height - panelH) / 2;

		String[] tabs = { "Visuals", "HUD", "Combat", "Movement", "Utility" };
		int tabW = 90, tabH = 22;
		int tabsTotal = tabs.length * tabW;
		int tabsX = x0 + panelW / 2 - tabsTotal / 2;
		int tabsY = y0 + 12;
		for (int i = 0; i < tabs.length; i++) {
			final String tab = tabs[i];
			addRenderableWidget(Button.builder(Component.literal(tab), b -> {
				activeTab = tab;
				rebuild();
			}).bounds(tabsX + i * tabW, tabsY, tabW - 4, tabH).build());
		}

		List<Toggle> items = new ArrayList<>();
		switch (activeTab) {
			case "Visuals":
				items.add(new Toggle("See Invisible", () -> cfg.видетьНевидимых, v -> cfg.видетьНевидимых = v));
				items.add(new Toggle("Custom Hitbox", () -> cfg.customHitbox, v -> cfg.customHitbox = v));
				items.add(new Toggle("Target ESP", () -> cfg.targetEsp, v -> cfg.targetEsp = v));
				items.add(new Toggle("Trap ESP", () -> cfg.trapEsp, v -> cfg.trapEsp = v));
				items.add(new Toggle("Item Highlighter", () -> cfg.itemHighlighter, v -> cfg.itemHighlighter = v));
				items.add(new Toggle("Friend Markers", () -> cfg.friendMarkers, v -> cfg.friendMarkers = v));
				items.add(new Toggle("Kill Effects", () -> cfg.killEffects, v -> cfg.killEffects = v));
				items.add(new Toggle("TNT Timer", () -> cfg.tntTimer, v -> cfg.tntTimer = v));
				items.add(new Toggle("Prediction", () -> cfg.prediction, v -> cfg.prediction = v));
				items.add(new Toggle("Sound ESP", () -> cfg.soundEsp, v -> cfg.soundEsp = v));
				items.add(new Toggle("Item Radius", () -> cfg.itemRadius, v -> cfg.itemRadius = v));
				break;
			case "HUD":
				items.add(new Toggle("Armor HUD", () -> cfg.armorHud, v -> cfg.armorHud = v));
				items.add(new Toggle("Target HUD", () -> cfg.targetHud, v -> cfg.targetHud = v));
				items.add(new Toggle("Potions", () -> cfg.potionsHud, v -> cfg.potionsHud = v));
				items.add(new Toggle("Effect Notify", () -> cfg.effectNotify, v -> cfg.effectNotify = v));
				items.add(new Toggle("Watermark", () -> cfg.watermarkHud, v -> cfg.watermarkHud = v));
				items.add(new Toggle("World Info", () -> cfg.worldHud, v -> cfg.worldHud = v));
				items.add(new Toggle("Player Info", () -> cfg.playerHud, v -> cfg.playerHud = v));
				items.add(new Toggle("Keybinds", () -> cfg.keybindsHud, v -> cfg.keybindsHud = v));
				items.add(new Toggle("Cooldowns", () -> cfg.cooldownsHud, v -> cfg.cooldownsHud = v));
				items.add(new Toggle("Dynamic Island", () -> cfg.dynamicIsland, v -> cfg.dynamicIsland = v));
				items.add(new Toggle("Inventory HUD", () -> cfg.inventoryHud, v -> cfg.inventoryHud = v));
				break;
			case "Combat":
				items.add(new Toggle("Hit Sound", () -> cfg.hitSound, v -> cfg.hitSound = v));
				items.add(new Toggle("Shift Tap", () -> cfg.shiftTap, v -> cfg.shiftTap = v));
				items.add(new Toggle("Auto Swap (Totem)", () -> cfg.autoSwap, v -> cfg.autoSwap = v));
				break;
			case "Movement":
				items.add(new Toggle("Auto Sprint", () -> cfg.autoSprint, v -> cfg.autoSprint = v));
				items.add(new Toggle("Auto Eat", () -> cfg.autoEat, v -> cfg.autoEat = v));
				items.add(new Toggle("Auto Invisible", () -> cfg.autoInvisible, v -> cfg.autoInvisible = v));
				items.add(new Toggle("Elytra Utils", () -> cfg.elytraUtils, v -> cfg.elytraUtils = v));
				break;
			case "Utility":
				items.add(new Toggle("Auto Accept TPA", () -> cfg.autoAccept, v -> cfg.autoAccept = v));
				items.add(new Toggle("Auto Near", () -> cfg.autoNear, v -> cfg.autoNear = v));
				items.add(new Toggle("Auto Join", () -> cfg.autoJoin, v -> cfg.autoJoin = v));
				items.add(new Toggle("Middle Click", () -> cfg.middleClick, v -> cfg.middleClick = v));
				items.add(new Toggle("Death Coords", () -> cfg.deathCoords, v -> cfg.deathCoords = v));
				items.add(new Toggle("Totem Tracker", () -> cfg.totemTracker, v -> cfg.totemTracker = v));
				items.add(new Toggle("Item Pickup Notify", () -> cfg.itemPickup, v -> cfg.itemPickup = v));
				break;
		}

		int contentY = tabsY + tabH + 18;
		int colW = panelW / 2 - 20;
		int rowH = 28;
		int leftX = x0 + 16;
		int rightX = x0 + panelW / 2 + 4;
		for (int i = 0; i < items.size(); i++) {
			Toggle t = items.get(i);
			int col = i % 2;
			int row = i / 2;
			t.x = (col == 0 ? leftX : rightX);
			t.y = contentY + row * rowH;
			t.w = colW;
			t.h = 22;
			addRenderableWidget(new ToggleButton(t));
		}
	}

	private void rebuild() {
		this.clearWidgets();
		this.init();
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		int panelW = 520;
		int panelH = 320;
		int x0 = (this.width - panelW) / 2;
		int y0 = (this.height - panelH) / 2;
		gui.fill(x0, y0, x0 + panelW, y0 + panelH, 0xE0151821);
		gui.fill(x0, y0, x0 + panelW, y0 + 1, 0xFF2A2E3A);
		gui.fill(x0, y0 + panelH - 1, x0 + panelW, y0 + panelH, 0xFF2A2E3A);
		gui.fill(x0, y0, x0 + 1, y0 + panelH, 0xFF2A2E3A);
		gui.fill(x0 + panelW - 1, y0, x0 + panelW, y0 + panelH, 0xFF2A2E3A);
		gui.drawString(this.font, "grum  >  " + activeTab, x0 + 12, y0 + 8, 0xFFFFFFFF, false);
		super.render(gui, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT) {
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		GrumConfig.get().save();
		if (this.minecraft != null) this.minecraft.setScreen(null);
	}

	@Override
	public boolean isPauseScreen() { return false; }

	private static class Toggle {
		final String label;
		final BooleanSupplier getter;
		final Consumer<Boolean> setter;
		int x, y, w, h;

		Toggle(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
			this.label = label;
			this.getter = getter;
			this.setter = setter;
		}
	}

	private static class ToggleButton extends Button {
		private final Toggle t;

		ToggleButton(Toggle t) {
			super(t.x, t.y, t.w, t.h, Component.literal(t.label),
					b -> { t.setter.accept(!t.getter.getAsBoolean()); GrumConfig.get().save(); },
					DEFAULT_NARRATION);
			this.t = t;
		}

		@Override
		protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
			int bg = isHovered() ? 0xFF1E2230 : 0xFF181B25;
			gui.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
			gui.fill(getX(), getY(), getX() + getWidth(), getY() + 1, 0xFF2A2E3A);
			gui.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), 0xFF2A2E3A);

			Minecraft mc = Minecraft.getInstance();
			gui.drawString(mc.font, t.label, getX() + 8, getY() + (getHeight() - 8) / 2, 0xFFE0E0E0, false);

			boolean on = t.getter.getAsBoolean();
			int sw = 24, sh = 12;
			int sx = getX() + getWidth() - sw - 8;
			int sy = getY() + (getHeight() - sh) / 2;
			int track = on ? GrumConfig.get().accentColor : 0xFF3A3F4B;
			gui.fill(sx, sy, sx + sw, sy + sh, track);
			int knobX = on ? sx + sw - sh : sx;
			gui.fill(knobX, sy, knobX + sh, sy + sh, 0xFFFFFFFF);
		}
	}
}
