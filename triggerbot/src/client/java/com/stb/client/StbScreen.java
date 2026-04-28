package com.stb.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Hidden config screen. Opened via Right-Shift+Right-Ctrl only. */
public class StbScreen extends Screen {
	private final Screen parent;
	private final StbConfig c = StbConfig.get();

	public StbScreen(Screen parent) {
		super(Component.literal("opts"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int w = 210;
		int left = cx - w - 5;
		int right = cx + 5;
		int y = 40;
		int rh = 24;

		addRenderableWidget(CycleButton.onOffBuilder(c.enabled)
				.create(left, y, w, 20, Component.literal("enabled"), (b, v) -> c.enabled = v));
		addRenderableWidget(CycleButton.onOffBuilder(c.waitCooldown)
				.create(left, y + rh, w, 20, Component.literal("wait cooldown"), (b, v) -> c.waitCooldown = v));
		addRenderableWidget(CycleButton.onOffBuilder(c.critOnly)
				.create(left, y + rh * 2, w, 20, Component.literal("crit only"), (b, v) -> c.critOnly = v));
		addRenderableWidget(CycleButton.onOffBuilder(c.requireWeapon)
				.create(left, y + rh * 3, w, 20, Component.literal("weapon only"), (b, v) -> c.requireWeapon = v));
		addRenderableWidget(CycleButton.onOffBuilder(c.requireLos)
				.create(left, y + rh * 4, w, 20, Component.literal("line of sight"), (b, v) -> c.requireLos = v));
		addRenderableWidget(CycleButton.onOffBuilder(c.notWhileUsingItem)
				.create(left, y + rh * 5, w, 20, Component.literal("not using item"), (b, v) -> c.notWhileUsingItem = v));
		addRenderableWidget(CycleButton.onOffBuilder(c.ignoreUtility)
				.create(left, y + rh * 6, w, 20, Component.literal("ignore utility"), (b, v) -> c.ignoreUtility = v));

		addRenderableWidget(CycleButton.onOffBuilder(c.targetHostile)
				.create(right, y, w, 20, Component.literal("hostile"), (b, v) -> c.targetHostile = v));
		addRenderableWidget(CycleButton.onOffBuilder(c.targetPassive)
				.create(right, y + rh, w, 20, Component.literal("passive"), (b, v) -> c.targetPassive = v));
		addRenderableWidget(CycleButton.onOffBuilder(c.targetPlayers)
				.create(right, y + rh * 2, w, 20, Component.literal("players"), (b, v) -> c.targetPlayers = v));

		addRenderableWidget(new IntSlider(right, y + rh * 3, w, 20,
				"interval ms", c.intervalMs, 0, 500, v -> c.intervalMs = v));
		addRenderableWidget(new IntSlider(right, y + rh * 4, w, 20,
				"jitter ms", c.jitterMs, 0, 300, v -> c.jitterMs = v));
		addRenderableWidget(new IntSlider(right, y + rh * 5, w, 20,
				"switch ms", c.switchDelayMs, 0, 400, v -> c.switchDelayMs = v));
		addRenderableWidget(new DblSlider(right, y + rh * 6, w, 20,
				"reach", c.reach, 2.0, 3.0, v -> c.reach = v));
		addRenderableWidget(new DblSlider(left, y + rh * 7, w, 20,
				"max angle", c.maxAngle, 20.0, 180.0, v -> c.maxAngle = v));
		addRenderableWidget(new DblSlider(right, y + rh * 7, w, 20,
				"random skip", c.randomSkip, 0.0, 0.3, v -> c.randomSkip = v));

		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
				.bounds(cx - 75, this.height - 28, 150, 20).build());
	}

	@Override
	public void render(GuiGraphics g, int mx, int my, float d) {
		super.render(g, mx, my, d);
		g.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
		g.drawCenteredString(this.font,
				Component.literal("rshift = toggle · rshift+rctrl = this menu"),
				this.width / 2, 24, 0xAAAAAA);
	}

	@Override
	public void onClose() {
		c.save();
		if (minecraft != null) minecraft.setScreen(parent);
	}

	private static class IntSlider extends AbstractSliderButton {
		private final String label;
		private final int min, max;
		private final java.util.function.IntConsumer setter;

		IntSlider(int x, int y, int w, int h, String label, int cur, int min, int max, java.util.function.IntConsumer setter) {
			super(x, y, w, h, Component.empty(), clamp01((cur - min) / (double) (max - min)));
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			int v = (int) Math.round(min + this.value * (max - min));
			setMessage(Component.literal(label + ": " + v));
		}

		@Override
		protected void applyValue() {
			int v = (int) Math.round(min + this.value * (max - min));
			setter.accept(v);
		}
	}

	private static class DblSlider extends AbstractSliderButton {
		private final String label;
		private final double min, max;
		private final java.util.function.DoubleConsumer setter;

		DblSlider(int x, int y, int w, int h, String label, double cur, double min, double max, java.util.function.DoubleConsumer setter) {
			super(x, y, w, h, Component.empty(), clamp01((cur - min) / (max - min)));
			this.label = label;
			this.min = min;
			this.max = max;
			this.setter = setter;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			double v = min + this.value * (max - min);
			setMessage(Component.literal(String.format("%s: %.2f", label, v)));
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
