package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Рисует отметку над головой друзей в 3D-мире. */
public final class FriendMarkers {
	private FriendMarkers() {}

	public static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;
		GrumConfig cfg = GrumConfig.get();
		if (cfg.friends.isEmpty()) return;

		Camera cam = ctx.camera();
		Vec3 camPos = cam.getPosition();
		PoseStack ps = ctx.matrixStack();
		MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
		Font font = mc.font;

		for (var e : mc.level.entitiesForRendering()) {
			if (!(e instanceof Player p) || p == mc.player) continue;
			String name = p.getGameProfile().getName();
			if (!cfg.friends.contains(name)) continue;

			String text = "★ " + name;
			ps.pushPose();
			ps.translate(p.getX() - camPos.x, p.getY() + p.getBbHeight() + 0.4 - camPos.y, p.getZ() - camPos.z);
			ps.mulPose(cam.rotation());
			ps.scale(-0.025f, -0.025f, 0.025f);
			Matrix4f m = ps.last().pose();
			int color = (cfg.accentColor & 0x00FFFFFF) | 0xFF000000;
			font.drawInBatch(Component.literal(text), -font.width(text) / 2.0f, 0, color, false, m, buf,
					Font.DisplayMode.SEE_THROUGH, 0x60000000, 0xF000F0);
			ps.popPose();
		}
		buf.endBatch();
	}
}
