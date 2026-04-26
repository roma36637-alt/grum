package com.example.grum.client.mixin;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
			at = @At("TAIL"))
	private void grum$forceVisible(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
		if (!grum$isSingleplayer()) {
			return;
		}
		if (GrumConfig.get().видетьНевидимых && state.isInvisible) {
			state.isInvisible = false;
		}
	}

	private static boolean grum$isSingleplayer() {
		Minecraft mc = Minecraft.getInstance();
		// Работает только в одиночной игре, не работает на серверах и в открытом LAN-мире
		if (!mc.hasSingleplayerServer()) return false;
		if (mc.getCurrentServer() != null) return false;
		var server = mc.getSingleplayerServer();
		if (server == null) return false;
		return !server.isPublished();
	}
}
