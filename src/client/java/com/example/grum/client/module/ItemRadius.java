package com.example.grum.client.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Рисует круг радиуса действия для предметов в руке (жемчуг ~32, эндер-сундук ~5 и т.п.). */
public final class ItemRadius {
	private ItemRadius() {}

	public static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		Player p = mc.player;
		double radius = radiusFor(p);
		if (radius <= 0) return;

		Vec3 camPos = ctx.camera().getPosition();
		PoseStack ps = ctx.matrixStack();
		MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
		VertexConsumer vc = buf.getBuffer(RenderType.lines());

		float r = 0.3f, g = 0.7f, b = 1f, a = 0.8f;

		ps.pushPose();
		ps.translate(p.getX() - camPos.x, p.getY() + 0.05 - camPos.y, p.getZ() - camPos.z);
		Matrix4f m = ps.last().pose();

		int segments = 64;
		float prevX = (float) radius, prevZ = 0f;
		for (int i = 1; i <= segments; i++) {
			double angle = (Math.PI * 2 * i) / segments;
			float x = (float) (Math.cos(angle) * radius);
			float z = (float) (Math.sin(angle) * radius);
			vc.addVertex(m, prevX, 0, prevZ).setColor(r, g, b, a).setNormal(0, 1, 0);
			vc.addVertex(m, x, 0, z).setColor(r, g, b, a).setNormal(0, 1, 0);
			prevX = x; prevZ = z;
		}
		ps.popPose();
		buf.endBatch(RenderType.lines());
	}

	private static double radiusFor(Player p) {
		var stack = p.getMainHandItem();
		if (stack.is(Items.ENDER_PEARL)) return 32.0;
		if (stack.is(Items.SNOWBALL) || stack.is(Items.EGG)) return 24.0;
		if (stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) return 8.0;
		if (stack.is(Items.TRIDENT)) return 28.0;
		if (stack.is(Items.BOW) || stack.is(Items.CROSSBOW)) return 24.0;
		return 0;
	}
}
