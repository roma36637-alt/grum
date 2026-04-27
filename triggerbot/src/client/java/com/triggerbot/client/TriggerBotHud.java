package com.triggerbot.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public final class TriggerBotHud {
	private TriggerBotHud() {}

	public static void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
		TriggerBotConfig cfg = TriggerBotClient.config;
		if (cfg == null || !cfg.showHud) return;
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options.hudHidden) return;
		if (client.getDebugHud().shouldShowDebugHud()) return;

		String state = cfg.enabled ? "§a[TB ON]" : "§8[TB OFF]";
		Text text = Text.literal(state);
		int x = 4;
		int y = 4;
		ctx.drawTextWithShadow(client.textRenderer, text, x, y, 0xFFFFFFFF);
	}
}
