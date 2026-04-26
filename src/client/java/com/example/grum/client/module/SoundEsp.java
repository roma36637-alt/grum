package com.example.grum.client.module;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Показывает на короткое время текстовый маркер на месте громких звуков (TNT, трезубец, и т.п.). */
public final class SoundEsp {
	private static final List<SoundMarker> markers = new ArrayList<>();
	private static final long DURATION_MS = 2500;

	private SoundEsp() {}

	public static void mark(double x, double y, double z, String label) {
		markers.add(new SoundMarker(x, y, z, label, System.currentTimeMillis()));
		if (markers.size() > 32) markers.remove(0);
	}

	public static void render(WorldRenderContext ctx) {
		if (markers.isEmpty()) return;
		long now = System.currentTimeMillis();
		markers.removeIf(m -> now - m.time > DURATION_MS);
		if (markers.isEmpty()) return;

		Minecraft mc = Minecraft.getInstance();
		Camera cam = ctx.camera();
		Vec3 camPos = cam.getPosition();
		PoseStack ps = ctx.matrixStack();
		MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
		Font font = mc.font;

		for (SoundMarker mk : markers) {
			ps.pushPose();
			ps.translate(mk.x - camPos.x, mk.y + 0.5 - camPos.y, mk.z - camPos.z);
			ps.mulPose(cam.rotation());
			ps.scale(-0.025f, -0.025f, 0.025f);
			Matrix4f m = ps.last().pose();
			font.drawInBatch(Component.literal(mk.label), -font.width(mk.label) / 2.0f, 0, 0xFFFFAA40, false, m, buf,
					Font.DisplayMode.SEE_THROUGH, 0x60000000, 0xF000F0);
			ps.popPose();
		}
		buf.endBatch();
	}

	private static class SoundMarker {
		final double x, y, z; final String label; final long time;
		SoundMarker(double x, double y, double z, String label, long time) {
			this.x = x; this.y = y; this.z = z; this.label = label; this.time = time;
		}
	}
}
