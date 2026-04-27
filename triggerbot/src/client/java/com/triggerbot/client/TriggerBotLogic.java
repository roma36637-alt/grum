package com.triggerbot.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class TriggerBotLogic {
	private long nextAttackAt = 0L;
	private final Random random = new Random();

	public void tick(MinecraftClient client) {
		TriggerBotConfig cfg = TriggerBotClient.config;
		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null) return;
		if (client.currentScreen != null) return;

		long now = System.currentTimeMillis();
		if (now < nextAttackAt) return;

		if (cfg.requireWeapon && !isWeapon(player.getMainHandStack())) return;

		if (cfg.waitForCooldown) {
			float progress = player.getAttackCooldownProgress(0.0f);
			if (progress < 1.0f) return;
		}

		if (cfg.critOnly) {
			boolean canCrit = player.fallDistance > 0.0f
					&& !player.isOnGround()
					&& !player.isClimbing()
					&& !player.isTouchingWater()
					&& !player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS)
					&& !player.hasVehicle();
			if (!canCrit) return;
		}

		Entity target = findTarget(client, player, cfg);
		if (target == null) return;
		if (!isAcceptableTarget(target, player, cfg)) return;

		// Attack
		if (client.interactionManager != null) {
			client.interactionManager.attackEntity(player, target);
		}
		player.swingHand(Hand.MAIN_HAND);

		int delay = Math.max(0, cfg.delayMs);
		if (cfg.jitterMs > 0) delay += random.nextInt(cfg.jitterMs + 1);
		nextAttackAt = now + delay;
	}

	private Entity findTarget(MinecraftClient client, ClientPlayerEntity player, TriggerBotConfig cfg) {
		// Use vanilla crosshair target first.
		HitResult xhair = client.crosshairTarget;
		if (xhair instanceof EntityHitResult ehr) {
			Entity e = ehr.getEntity();
			if (withinReach(player, e, cfg.reach)) return e;
		}

		if (cfg.hitboxExpand <= 0.0) return null;

		// Custom wider raycast (expanded hitbox) to catch near-misses.
		Vec3d eyes = player.getCameraPosVec(1.0f);
		Vec3d look = player.getRotationVec(1.0f);
		Vec3d end = eyes.add(look.multiply(cfg.reach));
		Box searchBox = player.getBoundingBox()
				.stretch(look.multiply(cfg.reach))
				.expand(1.0 + cfg.hitboxExpand);

		Entity best = null;
		double bestDist = Double.MAX_VALUE;
		for (Entity e : client.world.getOtherEntities(player, searchBox, entity -> !entity.isSpectator() && entity.canHit())) {
			Box box = e.getBoundingBox().expand(cfg.hitboxExpand);
			var hit = box.raycast(eyes, end);
			if (hit.isPresent()) {
				double d = eyes.squaredDistanceTo(hit.get());
				if (d < bestDist && Math.sqrt(d) <= cfg.reach) {
					bestDist = d;
					best = e;
				}
			}
		}
		return best;
	}

	private boolean withinReach(PlayerEntity player, Entity target, double reach) {
		Vec3d eyes = player.getCameraPosVec(1.0f);
		return target.squaredDistanceTo(eyes) <= reach * reach;
	}

	private boolean isAcceptableTarget(Entity e, PlayerEntity self, TriggerBotConfig cfg) {
		if (e == self) return false;
		if (e.isRemoved() || !e.isAlive()) return false;
		if (e instanceof ItemEntity) return false;
		if (cfg.ignoreUtilityEntities &&
				(e instanceof ArmorStandEntity || e instanceof ItemFrameEntity || e instanceof DisplayEntity)) {
			return false;
		}

		if (cfg.targetAny) return true;

		if (e instanceof PlayerEntity) return cfg.targetPlayers;
		if (e instanceof HostileEntity) return cfg.targetHostile;
		if (e instanceof AnimalEntity || e instanceof PassiveEntity) return cfg.targetPassive;
		if (e instanceof LivingEntity) return cfg.targetHostile; // other mobs (e.g. slime, ghast)
		return false;
	}

	private boolean isWeapon(ItemStack stack) {
		if (stack.isEmpty()) return false;
		var item = stack.getItem();
		if (item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem || item instanceof MaceItem) {
			return true;
		}
		return item == Items.BOW || item == Items.CROSSBOW;
	}
}
