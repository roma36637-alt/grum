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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Рисует кастомные хитбоксы вокруг живых сущностей. */
public final class CustomHitbox {
	private CustomHitbox() {}

	public static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;
		GrumConfig cfg = GrumConfig.get();

		Camera cam = ctx.camera();
		Vec3 camPos = cam.getPosition();
		PoseStack ps = ctx.matrixStack();
		MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
		VertexConsumer vc = buf.getBuffer(RenderType.lines());

		int color = (cfg.accentColor & 0x00FFFFFF) | 0xFF000000;
		float a = ((color >> 24) & 0xFF) / 255f;
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;

		for (Entity e : mc.level.entitiesForRendering()) {
			if (!(e instanceof LivingEntity)) continue;
			if (e == mc.player) continue;
			AABB bb = e.getBoundingBox();
			ps.pushPose();
			ps.translate(-camPos.x, -camPos.y, -camPos.z);
			Matrix4f m = ps.last().pose();
			drawBox(vc, m,
					(float) bb.minX, (float) bb.minY, (float) bb.minZ,
					(float) bb.maxX, (float) bb.maxY, (float) bb.maxZ,
					r, g, b, a);
			ps.popPose();
		}
		buf.endBatch(RenderType.lines());
	}

	private static void drawBox(VertexConsumer vc, Matrix4f m,
	                            float x1, float y1, float z1,
	                            float x2, float y2, float z2,
	                            float r, float g, float b, float a) {
		// 12 рёбер AABB
		// нижний прямоугольник
		line(vc, m, x1, y1, z1, x2, y1, z1, r, g, b, a);
		line(vc, m, x2, y1, z1, x2, y1, z2, r, g, b, a);
		line(vc, m, x2, y1, z2, x1, y1, z2, r, g, b, a);
		line(vc, m, x1, y1, z2, x1, y1, z1, r, g, b, a);
		// верхний
		line(vc, m, x1, y2, z1, x2, y2, z1, r, g, b, a);
		line(vc, m, x2, y2, z1, x2, y2, z2, r, g, b, a);
		line(vc, m, x2, y2, z2, x1, y2, z2, r, g, b, a);
		line(vc, m, x1, y2, z2, x1, y2, z1, r, g, b, a);
		// вертикальные
		line(vc, m, x1, y1, z1, x1, y2, z1, r, g, b, a);
		line(vc, m, x2, y1, z1, x2, y2, z1, r, g, b, a);
		line(vc, m, x2, y1, z2, x2, y2, z2, r, g, b, a);
		line(vc, m, x1, y1, z2, x1, y2, z2, r, g, b, a);
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
