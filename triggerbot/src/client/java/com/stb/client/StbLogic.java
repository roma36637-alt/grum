package com.stb.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/** Core trigger-bot logic. Called every client tick. */
public final class StbLogic {
	private static final Random R = new Random();
	private static long nextAllowed = 0L;
	private static int lastTargetId = Integer.MIN_VALUE;
	private static long lastSwitchAt = 0L;

	private StbLogic() {}

	public static void tick(Minecraft mc) {
		StbConfig c = StbConfig.get();

		// --- Panic key: HOME unloads bot for the rest of the session ---
		if (!c.unhooked) {
			long wh = mc.getWindow().getWindow();
			if (InputConstants.isKeyDown(wh, InputConstants.KEY_HOME)) {
				c.unhooked = true;
				c.enabled = false;
				return;
			}
		}
		if (c.unhooked) return;

		if (!c.enabled) return;
		LocalPlayer p = mc.player;
		if (p == null || mc.level == null || mc.gameMode == null) return;
		if (mc.screen != null) return;
		if (p.isSpectator() || !p.isAlive()) return;

		if (c.notWhileUsingItem && p.isUsingItem()) return;

		// spaceOnly: only attack while Space is held (manual crit timing)
		if (c.spaceOnly) {
			long wh = mc.getWindow().getWindow();
			if (!InputConstants.isKeyDown(wh, InputConstants.KEY_SPACE)) return;
		}

		// Weapon check via DataComponents — works with modded weapons too.
		if (c.requireWeapon && !hasAttackDamage(p.getMainHandItem())) return;

		// Smart cooldown: use 0.9 threshold when airborne (tighter timing),
		// 1.0 when on ground (guaranteed max damage).
		if (c.waitCooldown) {
			float threshold = p.onGround() ? 1.0f : 0.9f;
			if (p.getAttackStrengthScale(0.5f) < threshold) return;
		}

		// Smart crit check — mirrors the logic from analyzed mods.
		if (c.critOnly && !canCrit(mc, p)) return;

		long now = System.currentTimeMillis();
		if (now < nextAllowed) return;

		Entity target = pick(mc, c, p);
		if (target == null) {
			lastTargetId = Integer.MIN_VALUE;
			return;
		}

		// switch delay humanizer
		if (target.getId() != lastTargetId) {
			lastTargetId = target.getId();
			lastSwitchAt = now;
			if (c.switchDelayMs > 0) {
				nextAllowed = now + c.switchDelayMs;
				return;
			}
		} else if (c.switchDelayMs > 0 && now - lastSwitchAt < c.switchDelayMs) {
			return;
		}

		// random skip (human imperfection)
		if (c.randomSkip > 0 && R.nextDouble() < c.randomSkip) {
			nextAllowed = now + 30 + R.nextInt(40);
			return;
		}

		mc.gameMode.attack(p, target);
		p.swing(InteractionHand.MAIN_HAND);

		int jitter = c.jitterMs > 0 ? R.nextInt(c.jitterMs + 1) : 0;
		nextAllowed = now + Math.max(0, c.intervalMs) + jitter;
	}

	/** Checks for ATTACK_DAMAGE attribute via DataComponents — universal weapon detection. */
	private static boolean hasAttackDamage(ItemStack stack) {
		if (stack.isEmpty()) return false;
		ItemAttributeModifiers mods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
		if (mods == null) return false;
		return mods.modifiers().stream()
				.anyMatch(e -> e.attribute().equals(Attributes.ATTACK_DAMAGE));
	}

	/**
	 * Smart crit logic ported from analyzed mods:
	 * - Can crit if on a ladder (block material check)
	 * - Can crit if Jump is held AND on ground (about to jump)
	 * - Can crit if fallDistance > 0 AND not on ground (standard falling crit)
	 * - Cannot crit with Blindness or Levitation active
	 */
	private static boolean canCrit(Minecraft mc, LocalPlayer p) {
		// Check disabling effects
		boolean jumping = p.input.keyPresses.jump();
		if (!jumping) {
			if (p.hasEffect(MobEffects.BLINDNESS)) return false;
			if (p.hasEffect(MobEffects.LEVITATION)) return false;
		}

		// On a climbable block → allow (crit logic works on ladders/vines)
		if (p.onClimbable()) return true;

		boolean onGround = p.onGround();
		// Jump key held + space-only style check: about to leave ground
		if (!mc.options.keyJump.isDown() && onGround) return true;

		// Standard airborne crit: not on ground and fallDistance > 0
		if (!onGround && p.fallDistance > 0.0f) return true;

		return false;
	}

	private static Entity pick(Minecraft mc, StbConfig c, LocalPlayer p) {
		// Prefer entity under crosshair (vanilla-style trigger).
		HitResult xhr = mc.hitResult;
		if (xhr instanceof EntityHitResult ehr) {
			Entity e = ehr.getEntity();
			if (acceptable(e, p, c) && inReach(p, e, c.reach)) return e;
		}

		// Fall back: nearest acceptable entity under the look cone.
		double radius = Math.min(c.reach, 3.0);
		Vec3 eye = p.getEyePosition();
		Vec3 look = p.getLookAngle();
		AABB box = p.getBoundingBox().inflate(radius);

		List<Entity> near = mc.level.getEntities(p, box, e -> acceptable(e, p, c));
		double radSq = radius * radius;
		Entity best = null;
		double bestScore = Double.MAX_VALUE;
		for (Entity e : near) {
			double d = e.getBoundingBox().distanceToSqr(eye);
			if (d > radSq) continue;
			if (c.requireLos && !los(p, e)) continue;
			if (c.maxAngle < 180) {
				Vec3 to = e.getBoundingBox().getCenter().subtract(eye).normalize();
				double dot = look.dot(to);
				double a = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));
				if (a > c.maxAngle) continue;
			}
			if (best == null || d < bestScore) {
				bestScore = d;
				best = e;
			}
		}
		return best;
	}

	private static boolean inReach(LocalPlayer p, Entity e, double reach) {
		Vec3 eye = p.getEyePosition();
		double r = Math.min(reach, 3.0);
		return e.getBoundingBox().distanceToSqr(eye) <= r * r;
	}

	private static boolean los(Player p, Entity e) {
		Vec3 from = p.getEyePosition();
		AABB bb = e.getBoundingBox();
		Vec3[] pts = {
				bb.getCenter(),
				new Vec3(bb.getCenter().x, bb.maxY - 0.1, bb.getCenter().z),
				new Vec3(bb.getCenter().x, bb.minY + 0.1, bb.getCenter().z),
		};
		for (Vec3 to : pts) {
			HitResult r = p.level().clip(new ClipContext(
					from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
			if (r.getType() == HitResult.Type.MISS) return true;
		}
		return false;
	}

	private static boolean acceptable(Entity e, Player self, StbConfig c) {
		if (e == self) return false;
		if (!e.isAlive() || e.isRemoved()) return false;
		if (e.isInvulnerable()) return false;
		if (!(e instanceof LivingEntity)) return false;
		if (e instanceof ItemEntity) return false;
		if (c.ignoreUtility && (e instanceof ArmorStand || e instanceof ItemFrame)) return false;

		if (e instanceof Player) return c.targetPlayers;
		if (e instanceof Enemy) return c.targetHostile;
		if (e instanceof Animal) return c.targetPassive;
		return c.targetHostile; // fallback for generic monsters/slimes
	}
}
