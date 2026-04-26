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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Бьёт всех в радиусе. Без поворота головы — только удар, чтобы античит меньше палил.
 *  Античит всё равно может определить аномальный ритм атак. */
public final class KillAura {
	private static long nextAllowedAttack = 0;
	private static int lastTargetId = Integer.MIN_VALUE;

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
		if (cfg.killAuraWaitCooldown) {
			if (player.getAttackStrengthScale(0.0f) < 1.0f) return;
		} else {
			if (now < nextAllowedAttack) return;
		}

		if (target.getId() != lastTargetId) {
			lastTargetId = target.getId();
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
		nextAllowedAttack = now + cfg.killAuraIntervalMs;
	}

	private static Entity findTarget(Minecraft mc, GrumConfig cfg) {
		Player player = mc.player;
		double radius = cfg.killAuraRadius;
		double radSq = radius * radius;
		Vec3 eye = player.getEyePosition();
		AABB box = player.getBoundingBox().inflate(radius);

		List<Entity> nearby = mc.level.getEntities(player, box, e -> isCandidate(e, player, cfg));

		Entity best = null;
		double bestDist = radSq;
		for (Entity e : nearby) {
			double d = e.getBoundingBox().distanceToSqr(eye);
			if (d > radSq) continue;
			if (d < bestDist) {
				bestDist = d;
				best = e;
			}
		}
		return best;
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
