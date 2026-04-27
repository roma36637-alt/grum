package com.triggerbot.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class TriggerBotConfigScreen extends Screen {
	private final Screen parent;
	private final TriggerBotConfig cfg = TriggerBotClient.config;

	public TriggerBotConfigScreen(Screen parent) {
		super(Text.literal("TriggerBot — настройки"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int colW = 200;
		int leftX = cx - colW - 5;
		int rightX = cx + 5;
		int y = 40;
		int rowH = 24;

		// Left column
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.enabled)
				.build(leftX, y, colW, 20, Text.literal("Включён"), (b, v) -> cfg.enabled = v));
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.waitForCooldown)
				.build(leftX, y + rowH, colW, 20, Text.literal("Ждать перезарядки"), (b, v) -> cfg.waitForCooldown = v));
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.critOnly)
				.build(leftX, y + rowH * 2, colW, 20, Text.literal("Только криты"), (b, v) -> cfg.critOnly = v));
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.requireWeapon)
				.build(leftX, y + rowH * 3, colW, 20, Text.literal("Только с оружием"), (b, v) -> cfg.requireWeapon = v));
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.ignoreUtilityEntities)
				.build(leftX, y + rowH * 4, colW, 20, Text.literal("Игнорировать стойки/рамки"), (b, v) -> cfg.ignoreUtilityEntities = v));
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.showHud)
				.build(leftX, y + rowH * 5, colW, 20, Text.literal("HUD индикатор"), (b, v) -> cfg.showHud = v));

		// Right column — targets
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.targetHostile)
				.build(rightX, y, colW, 20, Text.literal("Цели: враждебные"), (b, v) -> cfg.targetHostile = v));
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.targetPassive)
				.build(rightX, y + rowH, colW, 20, Text.literal("Цели: мирные"), (b, v) -> cfg.targetPassive = v));
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.targetPlayers)
				.build(rightX, y + rowH * 2, colW, 20, Text.literal("Цели: игроки"), (b, v) -> cfg.targetPlayers = v));
		this.addDrawableChild(CyclingButtonWidget.onOffBuilder(cfg.targetAny)
				.build(rightX, y + rowH * 3, colW, 20, Text.literal("Цели: любые"), (b, v) -> cfg.targetAny = v));

		// Sliders
		this.addDrawableChild(new IntSlider(rightX, y + rowH * 4, colW, 20,
				"Задержка", cfg.delayMs, 0, 500, v -> cfg.delayMs = v));
		this.addDrawableChild(new IntSlider(rightX, y + rowH * 5, colW, 20,
				"Джиттер", cfg.jitterMs, 0, 200, v -> cfg.jitterMs = v));
		this.addDrawableChild(new DoubleSlider(leftX, y + rowH * 6, colW, 20,
				"Дальность", cfg.reach, 2.0, 6.0, v -> cfg.reach = v));
		this.addDrawableChild(new DoubleSlider(rightX, y + rowH * 6, colW, 20,
				"Расш. хитбокс", cfg.hitboxExpand, 0.0, 1.0, v -> cfg.hitboxExpand = v));

		// Done button
		this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, b -> this.close())
				.dimensions(cx - 75, this.height - 28, 150, 20).build());
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFFFF);
		ctx.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("§7V — переключить • R-Shift — открыть меню"),
				this.width / 2, 26, 0xFFAAAAAA);
	}

	@Override
	public void close() {
		cfg.save();
		if (this.client != null) {
			this.client.setScreen(parent);
		}
	}

	// --- sliders ---

	private static class IntSlider extends SliderWidget {
		private final String label;
		private final int min, max;
		private final java.util.function.IntConsumer setter;

		IntSlider(int x, int y, int w, int h, String label, int current, int min, int max, java.util.function.IntConsumer setter) {
			super(x, y, w, h, Text.empty(), clamp01((current - min) / (double) (max - min)));
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			int v = (int) Math.round(min + this.value * (max - min));
			setMessage(Text.literal(label + ": " + v));
		}

		@Override
		protected void applyValue() {
			int v = (int) Math.round(min + this.value * (max - min));
			setter.accept(v);
		}
	}

	private static class DoubleSlider extends SliderWidget {
		private final String label;
		private final double min, max;
		private final java.util.function.DoubleConsumer setter;

		DoubleSlider(int x, int y, int w, int h, String label, double current, double min, double max, java.util.function.DoubleConsumer setter) {
			super(x, y, w, h, Text.empty(), clamp01((current - min) / (max - min)));
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			double v = min + this.value * (max - min);
			setMessage(Text.literal(String.format("%s: %.2f", label, v)));
		}

		@Override
		protected void applyValue() {
			double v = min + this.value * (max - min);
			setter.accept(v);
		}
	}

	private static double clamp01(double v) {
		if (Double.isNaN(v)) return 0.0;
		return Math.max(0.0, Math.min(1.0, v));
	}
}
