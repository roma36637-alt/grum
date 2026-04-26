package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Когда игрок планирует на элитрах — автоматически использует фейерверк для буста. */
public final class ElytraUtils {
	private static long lastBoost = 0;

	private ElytraUtils() {}

	public static void tick(Minecraft mc) {
		Player p = mc.player;
		if (p == null || mc.gameMode == null) return;
		if (!p.isFallFlying()) return;
		if (System.currentTimeMillis() - lastBoost < 5000) return;

		// Проверим что в надетых грудниках элитра
		ItemStack chest = p.getItemBySlot(EquipmentSlot.CHEST);
		if (!chest.is(Items.ELYTRA)) return;

		// Найти ракету в хотбаре или оффхенде
		InteractionHand hand = null;
		int rocketSlot = -1;
		if (p.getOffhandItem().is(Items.FIREWORK_ROCKET)) hand = InteractionHand.OFF_HAND;
		else {
			for (int i = 0; i < 9; i++) {
				if (p.getInventory().getItem(i).is(Items.FIREWORK_ROCKET)) {
					rocketSlot = i;
					hand = InteractionHand.MAIN_HAND;
					break;
				}
			}
		}
		if (hand == null) return;

		int savedSlot = p.getInventory().getSelectedSlot();
		if (rocketSlot >= 0) p.getInventory().setSelectedSlot(rocketSlot);
		mc.gameMode.useItem(p, hand);
		if (rocketSlot >= 0) p.getInventory().setSelectedSlot(savedSlot);
		lastBoost = System.currentTimeMillis();
	}
}
