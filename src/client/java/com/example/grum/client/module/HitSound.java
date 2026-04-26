package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class HitSound {
	private HitSound() {}

	public static void onAttack(Entity target) {
		if (!(target instanceof LivingEntity)) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		mc.player.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 0.7f, 1.4f);
	}
}
