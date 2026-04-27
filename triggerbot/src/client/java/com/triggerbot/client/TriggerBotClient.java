package com.triggerbot.client;

import com.triggerbot.TriggerBotMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TriggerBotClient implements ClientModInitializer {
	public static TriggerBotConfig config;
	public static KeyBinding toggleKey;
	public static KeyBinding configKey;

	private static TriggerBotLogic logic;

	@Override
	public void onInitializeClient() {
		config = TriggerBotConfig.load();

		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.triggerbot.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_V,
				"category.triggerbot"
		));

		configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.triggerbot.config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.triggerbot"
		));

		logic = new TriggerBotLogic();

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		HudRenderCallback.EVENT.register(TriggerBotHud::onHudRender);

		TriggerBotMod.LOGGER.info("TriggerBot client initialized");
	}

	private void onEndTick(MinecraftClient client) {
		while (toggleKey.wasPressed()) {
			config.enabled = !config.enabled;
			config.save();
			if (client.player != null) {
				client.player.sendMessage(
						Text.literal("§6[TriggerBot] §fстатус: " + (config.enabled ? "§aВКЛ" : "§cВЫКЛ")),
						true
				);
			}
		}

		while (configKey.wasPressed()) {
			client.setScreen(new TriggerBotConfigScreen(null));
		}

		if (config.enabled) {
			logic.tick(client);
		}
	}
}
