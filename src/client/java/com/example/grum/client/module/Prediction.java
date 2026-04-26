package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Рисует траекторию полёта снарядов (жемчуг, стрелы, трезубцы, снежки, зелья). */
public final class Prediction {
	private Prediction() {}

	public static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Vec3 camPos = ctx.camera().getPosition();
		PoseStack ps = ctx.matrixStack();
		MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
		VertexConsumer vc = buf.getBuffer(RenderType.lines());

		int color = (GrumConfig.get().accentColor & 0x00FFFFFF) | 0xFF000000;
		float a = 1f;
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;

		ps.pushPose();
		ps.translate(-camPos.x, -camPos.y, -camPos.z);
		Matrix4f m = ps.last().pose();

		for (Entity e : mc.level.entitiesForRendering()) {
			if (!shouldTrack(e)) continue;
			Vec3 pos = e.position();
			Vec3 vel = e.getDeltaMovement();
			float gravity = gravityFor(e);
			float drag = dragFor(e);

			double x = pos.x, y = pos.y, z = pos.z;
			double vx = vel.x, vy = vel.y, vz = vel.z;
			double prevX = x, prevY = y, prevZ = z;
			for (int step = 0; step < 80; step++) {
				prevX = x; prevY = y; prevZ = z;
				x += vx; y += vy; z += vz;
				vy -= gravity;
				vx *= drag; vy *= drag; vz *= drag;
				line(vc, m, (float)prevX, (float)prevY, (float)prevZ, (float)x, (float)y, (float)z, r, g, b, a);
			}
		}
		ps.popPose();
		buf.endBatch(RenderType.lines());
	}

	private static boolean shouldTrack(Entity e) {
		return e instanceof ThrowableProjectile || e instanceof AbstractArrow || e instanceof ThrownTrident;
	}

	private static float gravityFor(Entity e) {
		if (e instanceof AbstractArrow || e instanceof ThrownTrident) return 0.05f;
		return 0.03f; // throwables
	}

	private static float dragFor(Entity e) {
		if (e.isInWater()) return 0.6f;
		return 0.99f;
	}

	private static void line(VertexConsumer vc, Matrix4f m,
	                         float x1, float y1, float z1, float x2, float y2, float z2,
	                         float r, float g, float b, float a) {
		float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len == 0) return;
		dx /= len; dy /= len; dz /= len;
		vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setNormal(dx, dy, dz);
		vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setNormal(dx, dy, dz);
	}
}
