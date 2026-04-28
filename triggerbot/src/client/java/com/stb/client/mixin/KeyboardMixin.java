package com.stb.client.mixin;

import com.stb.client.StbConfig;
import com.stb.client.StbScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts key press BEFORE Minecraft's own key handling.
 * Right-Shift toggles enabled; Right-Shift + Right-Ctrl opens config screen.
 * Nothing is registered in Minecraft's key-bindings menu.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardMixin {

	@Inject(method = "keyPress", at = @At("HEAD"))
	private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
		if (action != GLFW.GLFW_PRESS) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		// Don't fire while any screen is open — prevents toggling during inventory/chat
		// and preserves the parent screen on the config screen.
		if (mc.screen != null) return;

		if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
			if (ctrl) {
				// Right-Shift + Ctrl → open hidden config screen
				mc.execute(() -> mc.setScreen(new StbScreen(null)));
			} else {
				// Right-Shift only → toggle enabled
				StbConfig c = StbConfig.get();
				c.enabled = !c.enabled;
				c.save();
			}
		}
	}
}
