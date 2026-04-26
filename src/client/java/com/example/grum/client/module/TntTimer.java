package com.example.grum.client.module;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

/** Рисует таймер до взрыва над динамитом. */
public final class TntTimer {
	private TntTimer() {}

	public static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;
		Camera cam = ctx.camera();
		Vec3 camPos = cam.getPosition();
		PoseStack ps = ctx.matrixStack();
		MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
		Font font = mc.font;

		for (var e : mc.level.entitiesForRendering()) {
			if (!(e instanceof PrimedTnt tnt)) continue;
			float seconds = tnt.getFuse() / 20.0f;
			String text = String.format("%.1f", seconds);

			ps.pushPose();
			ps.translate(tnt.getX() - camPos.x, tnt.getY() + 1.2 - camPos.y, tnt.getZ() - camPos.z);
			ps.mulPose(cam.rotation());
			ps.scale(-0.025f, -0.025f, 0.025f);
			Matrix4f m = ps.last().pose();
			int color = seconds < 1.0f ? 0xFFFF4040 : 0xFFFFFFFF;
			font.drawInBatch(Component.literal(text), -font.width(text) / 2.0f, 0, color, false, m, buf,
					Font.DisplayMode.SEE_THROUGH, 0x40000000, 0xF000F0);
			ps.popPose();
		}
		buf.endBatch();
	}
}
