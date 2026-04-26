package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

/** Запоминает у кого был тотем в руке/оффхенде и сообщает в чат когда тотем исчезает (= использован). */
public final class TotemTracker {
	private static final Map<Integer, String> hadTotem = new HashMap<>();

	private TotemTracker() {}

	public static void tick(Minecraft mc) {
		if (mc.level == null || mc.player == null) return;
		for (var e : mc.level.entitiesForRendering()) {
			if (!(e instanceof LivingEntity le) || e == mc.player) continue;
			boolean has = le.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || le.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
			boolean had = hadTotem.containsKey(le.getId());
			String name = le.getDisplayName().getString();
			if (has) {
				hadTotem.put(le.getId(), name);
			} else if (had && (le instanceof Player || !le.isAlive())) {
				hadTotem.remove(le.getId());
				if (le.isAlive() && le.getHealth() < le.getMaxHealth() / 2.0f) {
					mc.gui.getChat().addMessage(Component.literal("[grum] " + name + " снёс тотем"));
				}
			}
		}
	}
}
