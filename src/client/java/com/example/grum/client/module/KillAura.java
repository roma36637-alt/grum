package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/** Килаура с антипалевными настройками: ванильный reach, line-of-sight, рандом интервалов. */
public final class KillAura {
	private static final Random RNG = new Random();
	private static long nextAllowedAttack = 0;
	private static int lastTargetId = Integer.MIN_VALUE;
	private static long targetSwitchedAt = 0;

	private KillAura() {}

	public static void tick(Minecraft mc) {
		Player player = mc.player;
		if (player == null || mc.level == null || mc.gameMode == null) return;
		if (mc.screen != null) return;
		if (player.isSpectator() || !player.isAlive()) return;

		GrumConfig cfg = GrumConfig.get();
		if (cfg.killAuraRequireWeapon) {
			ItemStack mainHand = player.getMainHandItem();
			if (mainHand.isEmpty()) return;
			if (!mainHand.is(ItemTags.SWORDS) && !mainHand.is(ItemTags.AXES)) return;
		}
		if (cfg.killAuraNotWhileUsingItem && player.isUsingItem()) return;

		Entity target = findTarget(mc, cfg);
		if (target == null) {
			lastTargetId = Integer.MIN_VALUE;
			return;
		}

		long now = System.currentTimeMillis();
		// 1. ждём ванильный кулдаун
		if (cfg.killAuraWaitCooldown && player.getAttackStrengthScale(0.0f) < 1.0f) return;
		// 2. соблюдаем минимальный интервал с джиттером
		if (now < nextAllowedAttack) return;

		// 3. задержка после смены цели (имитация "перевод глаз")
		if (target.getId() != lastTargetId) {
			lastTargetId = target.getId();
			targetSwitchedAt = now;
			if (cfg.killAuraSwitchDelayMs > 0) return;
		} else if (cfg.killAuraSwitchDelayMs > 0 && now - targetSwitchedAt < cfg.killAuraSwitchDelayMs) {
			return;
		}

		if (cfg.killAuraStopSprint && player.isSprinting()) {
			if (mc.getConnection() != null) {
				mc.getConnection().send(new ServerboundPlayerCommandPacket(
						player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
			}
			player.setSprinting(false);
		}

		mc.gameMode.attack(player, target);
		player.swing(InteractionHand.MAIN_HAND);

		// рандомизация следующего интервала: base + 0..jitter
		int jitter = cfg.killAuraJitterMs > 0 ? RNG.nextInt(cfg.killAuraJitterMs + 1) : 0;
		nextAllowedAttack = now + cfg.killAuraIntervalMs + jitter;
	}

	private static Entity findTarget(Minecraft mc, GrumConfig cfg) {
		Player player = mc.player;
		double radius = Math.min(cfg.killAuraRadius, 3.0); // не превышаем ванильный reach
		double radSq = radius * radius;
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		AABB box = player.getBoundingBox().inflate(radius);

		List<Entity> nearby = mc.level.getEntities(player, box, e -> isCandidate(e, player, cfg));

		Entity best = null;
		double bestScore = Double.MAX_VALUE;
		for (Entity e : nearby) {
			double d = e.getBoundingBox().distanceToSqr(eye);
			if (d > radSq) continue;
			// 4. line-of-sight через стены
			if (cfg.killAuraRequireLineOfSight && !canSee(player, e)) continue;
			// 5. FOV-фильтр — игнорируем цели позади
			if (cfg.killAuraMaxAngle < 180) {
				Vec3 toTarget = e.getBoundingBox().getCenter().subtract(eye).normalize();
				double dot = look.dot(toTarget);
				double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, dot))));
				if (angle > cfg.killAuraMaxAngle) continue;
			}
			// приоритет: ближайший, при равенстве — фронтальный
			double score = d;
			if (best == null || score < bestScore) {
				bestScore = score;
				best = e;
			}
		}
		return best;
	}

	private static boolean canSee(Player player, Entity target) {
		Vec3 from = player.getEyePosition();
		AABB bb = target.getBoundingBox();
		Vec3[] points = {
				bb.getCenter(),
				new Vec3(bb.getCenter().x, bb.maxY - 0.1, bb.getCenter().z),
				new Vec3(bb.getCenter().x, bb.minY + 0.1, bb.getCenter().z)
		};
		for (Vec3 to : points) {
			HitResult res = player.level().clip(new ClipContext(
					from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
			if (res.getType() == HitResult.Type.MISS) return true;
		}
		return false;
	}

	private static boolean isCandidate(Entity e, Player self, GrumConfig cfg) {
		if (e == self) return false;
		if (!e.isAlive()) return false;
		if (!(e instanceof LivingEntity living)) return false;
		if (e.isInvulnerable() || e.isSpectator()) return false;
		if (living.getHealth() <= 0.0f) return false;

		if (e instanceof Player p) {
			if (!cfg.killAuraAttackPlayers) return false;
			if (p.isCreative()) return false;
			if (cfg.killAuraNotFriends && GrumConfig.get().friends.contains(p.getGameProfile().getName())) return false;
		} else {
			if (!cfg.killAuraAttackMobs) return false;
			if (cfg.killAuraNotPassive && isPassive(e)) return false;
		}
		return true;
	}

	private static boolean isPassive(Entity entity) {
		if (entity instanceof Enemy) return false;
		if (entity instanceof Animal) return true;
		if (entity instanceof AbstractVillager) return true;
		if (entity instanceof IronGolem golem) return golem.getTarget() == null;
		if (entity instanceof PathfinderMob mob) return mob.getTarget() == null;
		return false;
	}
}
