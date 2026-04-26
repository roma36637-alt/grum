package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/** Спавнит частицы (взрыв + пламя) на месте убитой сущности. */
public final class KillEffects {
	private static final Map<Integer, double[]> tracked = new HashMap<>();

	private KillEffects() {}

	public static void tick(Minecraft mc) {
		ClientLevel level = mc.level;
		if (level == null || mc.player == null) return;

		Map<Integer, double[]> current = new HashMap<>();
		for (var e : level.entitiesForRendering()) {
			if (!(e instanceof LivingEntity le) || e == mc.player) continue;
			if (le.getHealth() > 0) {
				current.put(le.getId(), new double[]{ le.getX(), le.getY() + le.getBbHeight() / 2.0, le.getZ() });
			}
		}

		for (var entry : tracked.entrySet()) {
			if (!current.containsKey(entry.getKey())) {
				double[] pos = entry.getValue();
				for (int i = 0; i < 12; i++) {
					double dx = (Math.random() - 0.5) * 0.5;
					double dy = (Math.random() - 0.5) * 0.5;
					double dz = (Math.random() - 0.5) * 0.5;
					level.addParticle(ParticleTypes.EXPLOSION, pos[0], pos[1], pos[2], dx, dy, dz);
					level.addParticle(ParticleTypes.FLAME, pos[0], pos[1], pos[2], dx * 0.3, dy * 0.3, dz * 0.3);
				}
			}
		}

		tracked.clear();
		tracked.putAll(current);
	}
}
