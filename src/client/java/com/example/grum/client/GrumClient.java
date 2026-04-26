package com.example.grum.client;

import com.example.grum.client.menu.GrumMenuScreen;
import com.example.grum.client.module.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionResult;

public class GrumClient implements ClientModInitializer {
	private static KeyMapping openMenuKey;

	@Override
	public void onInitializeClient() {
		GrumConfig.get();

		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.grum.open_menu",
				InputConstants.Type.KEYSYM, InputConstants.KEY_RSHIFT,
				"category.grum"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new GrumMenuScreen());
				}
			}
			GrumConfig cfg = GrumConfig.get();
			EffectNotify.tick(client);
			ShiftTap.tick(client);
			if (cfg.autoSprint) AutoSprint.tick(client);
			if (cfg.autoEat) AutoEat.tick(client);
			if (cfg.autoSwap) AutoSwap.tick(client);
			if (cfg.deathCoords) DeathCoords.tick(client);
			if (cfg.totemTracker) TotemTracker.tick(client);
			if (cfg.itemPickup) ItemPickup.tick(client);
		});

		HudRenderCallback.EVENT.register((gui, tracker) -> {
			GrumConfig cfg = GrumConfig.get();
			if (cfg.armorHud) ArmorHud.render(gui);
			if (cfg.targetHud) TargetHud.render(gui);
			if (cfg.potionsHud) PotionsHud.render(gui);
			if (cfg.effectNotify) EffectNotify.render(gui);
			if (cfg.watermarkHud) WatermarkHud.render(gui);
			if (cfg.worldHud) WorldHud.render(gui);
			if (cfg.playerHud) PlayerHud.render(gui);
			if (cfg.keybindsHud) KeybindsHud.render(gui);
			if (cfg.cooldownsHud) CooldownsHud.render(gui);
			if (cfg.itemHighlighter) ItemHighlighter.render(gui);
		});

		WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
			GrumConfig cfg = GrumConfig.get();
			if (cfg.tntTimer) TntTimer.render(ctx);
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			GrumConfig cfg = GrumConfig.get();
			if (cfg.shiftTap) ShiftTap.onAttack(entity);
			if (cfg.hitSound) HitSound.onAttack(entity);
			return InteractionResult.PASS;
		});

		ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
			if (overlay) return;
			GrumConfig cfg = GrumConfig.get();
			if (cfg.autoAccept) AutoAccept.onChat(message.getString());
		});
	}
}
