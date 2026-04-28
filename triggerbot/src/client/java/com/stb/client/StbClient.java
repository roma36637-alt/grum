package com.stb.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Stealth entrypoint: no KeyBindings, no HUD callbacks, no chat hooks.
 * Only registers an END_CLIENT_TICK handler that drives the trigger logic.
 * Key detection happens in KeyboardMixin so our binding is invisible
 * in Minecraft's controls menu.
 */
public class StbClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Preload config silently.
		StbConfig.get();
		ClientTickEvents.END_CLIENT_TICK.register(StbLogic::tick);
	}
}
