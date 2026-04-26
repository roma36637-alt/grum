package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Рисует уголковый маркер на сущности, на которую наведён прицел. Стиль Fever Visual. */
public final class TargetEsp {
	private static LivingEntity lastTarget;
	private static long lastSeen;

	private TargetEsp() {}

	public static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;

		LivingEntity target = currentTarget(mc);
		long now = System.currentTimeMillis();
		if (target != null) {
			lastTarget = target;
			lastSeen = now;
		}
		if (lastTarget == null || !lastTarget.isAlive() || now - lastSeen > 800) return;

		Camera cam = ctx.camera();
		Vec3 camPos = cam.getPosition();
		PoseStack ps = ctx.matrixStack();
		MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
		VertexConsumer vc = buf.getBuffer(RenderType.lines());

		int color = (GrumConfig.get().accentColor & 0x00FFFFFF) | 0xFF000000;
		float a = 1f;
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;

		AABB bb = lastTarget.getBoundingBox();
		ps.pushPose();
		ps.translate(-camPos.x, -camPos.y, -camPos.z);
		Matrix4f m = ps.last().pose();
		drawCornerBrackets(vc, m,
				(float) bb.minX, (float) bb.minY, (float) bb.minZ,
				(float) bb.maxX, (float) bb.maxY, (float) bb.maxZ,
				r, g, b, a);
		ps.popPose();
		buf.endBatch(RenderType.lines());
	}

	private static LivingEntity currentTarget(Minecraft mc) {
		if (mc.hitResult instanceof EntityHitResult ehr) {
			Entity e = ehr.getEntity();
			if (e instanceof LivingEntity le && e != mc.player) return le;
		}
		return null;
	}

	/** Рисует только короткие уголки в каждой вершине бокса (стиль "цель"). */
	private static void drawCornerBrackets(VertexConsumer vc, Matrix4f m,
	                                       float x1, float y1, float z1,
	                                       float x2, float y2, float z2,
	                                       float r, float g, float b, float a) {
		float lx = (x2 - x1) * 0.25f;
		float ly = (y2 - y1) * 0.20f;
		float lz = (z2 - z1) * 0.25f;
		float[][] corners = {
				{ x1, y1, z1, +1, +1, +1 },
				{ x2, y1, z1, -1, +1, +1 },
				{ x1, y1, z2, +1, +1, -1 },
				{ x2, y1, z2, -1, +1, -1 },
				{ x1, y2, z1, +1, -1, +1 },
				{ x2, y2, z1, -1, -1, +1 },
				{ x1, y2, z2, +1, -1, -1 },
				{ x2, y2, z2, -1, -1, -1 }
		};
		for (float[] c : corners) {
			float x = c[0], y = c[1], z = c[2];
			float sx = c[3], sy = c[4], sz = c[5];
			line(vc, m, x, y, z, x + sx * lx, y, z, r, g, b, a);
			line(vc, m, x, y, z, x, y + sy * ly, z, r, g, b, a);
			line(vc, m, x, y, z, x, y, z + sz * lz, r, g, b, a);
		}
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
