package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** При клике колёсиком мыши — выполняет настроенное действие на цели под прицелом. */
public final class MiddleClick {
	private static boolean wasPressed = false;

	private MiddleClick() {}

	public static void tick(Minecraft mc) {
		if (mc.player == null) return;
		boolean pressed = org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().getWindow(),
				org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == 1;
		if (pressed && !wasPressed && mc.screen == null) {
			perform(mc);
		}
		wasPressed = pressed;
	}

	private static void perform(Minecraft mc) {
		String action = GrumConfig.get().middleClickAction;
		if (action == null) action = "friend";
		if (mc.hitResult instanceof EntityHitResult ehr) {
			Entity e = ehr.getEntity();
			if (e instanceof net.minecraft.world.entity.player.Player p) {
				String name = p.getGameProfile().getName();
				switch (action) {
					case "friend":
						var list = GrumConfig.get().friends;
						if (list.contains(name)) list.remove(name);
						else list.add(name);
						GrumConfig.get().save();
						mc.gui.getChat().addMessage(net.minecraft.network.chat.Component.literal(
								"[grum] " + name + (list.contains(name) ? " добавлен в друзей" : " убран из друзей")));
						break;
					case "msg":
						mc.player.connection.sendCommand("msg " + name + " hi");
						break;
				}
			} else if (e instanceof LivingEntity le) {
				mc.gui.getChat().addMessage(net.minecraft.network.chat.Component.literal(
						"[grum] " + le.getDisplayName().getString() + " HP: " + le.getHealth() + "/" + le.getMaxHealth()));
			}
		}
	}
}
