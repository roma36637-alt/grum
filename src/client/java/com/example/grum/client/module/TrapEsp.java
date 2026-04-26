package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Подсвечивает опасные блоки (паутина, нити, тнт, магма) в радиусе вокруг игрока. */
public final class TrapEsp {
	private static final int RADIUS = 16;

	private TrapEsp() {}

	public static void render(WorldRenderContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;
		GrumConfig cfg = GrumConfig.get();

		Camera cam = ctx.camera();
		Vec3 camPos = cam.getPosition();
		PoseStack ps = ctx.matrixStack();
		MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();
		VertexConsumer vc = buf.getBuffer(RenderType.lines());

		int color = 0xFFFF4040;
		float a = 1f, r = 1f, g = 0.25f, b = 0.25f;

		BlockPos center = mc.player.blockPosition();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		ps.pushPose();
		ps.translate(-camPos.x, -camPos.y, -camPos.z);
		Matrix4f m = ps.last().pose();

		int found = 0;
		for (int dx = -RADIUS; dx <= RADIUS; dx++) {
			for (int dy = -RADIUS; dy <= RADIUS; dy++) {
				for (int dz = -RADIUS; dz <= RADIUS; dz++) {
					pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					BlockState s = mc.level.getBlockState(pos);
					if (isTrap(s)) {
						drawCube(vc, m, pos.getX(), pos.getY(), pos.getZ(), r, g, b, a);
						if (++found > 200) break;
					}
				}
			}
		}
		ps.popPose();
		buf.endBatch(RenderType.lines());
	}

	private static boolean isTrap(BlockState s) {
		return s.is(Blocks.COBWEB) || s.is(Blocks.TRIPWIRE) || s.is(Blocks.TRIPWIRE_HOOK)
				|| s.is(Blocks.TNT) || s.is(Blocks.MAGMA_BLOCK) || s.is(Blocks.SOUL_SAND)
				|| s.is(Blocks.LAVA) || s.is(Blocks.SWEET_BERRY_BUSH) || s.is(Blocks.WITHER_ROSE);
	}

	private static void drawCube(VertexConsumer vc, Matrix4f m, int x, int y, int z, float r, float g, float b, float a) {
		float x1 = x, y1 = y, z1 = z;
		float x2 = x + 1f, y2 = y + 1f, z2 = z + 1f;
		// 12 edges
		line(vc, m, x1, y1, z1, x2, y1, z1, r, g, b, a);
		line(vc, m, x2, y1, z1, x2, y1, z2, r, g, b, a);
		line(vc, m, x2, y1, z2, x1, y1, z2, r, g, b, a);
		line(vc, m, x1, y1, z2, x1, y1, z1, r, g, b, a);
		line(vc, m, x1, y2, z1, x2, y2, z1, r, g, b, a);
		line(vc, m, x2, y2, z1, x2, y2, z2, r, g, b, a);
		line(vc, m, x2, y2, z2, x1, y2, z2, r, g, b, a);
		line(vc, m, x1, y2, z2, x1, y2, z1, r, g, b, a);
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
