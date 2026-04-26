package com.example.grum.client.module;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;

/** Автоматически выпивает зелье невидимости когда HP падает ниже 50% и эффект не активен. */
public final class AutoInvisible {
	private static int savedSlot = -1;
	private static boolean drinking = false;
	private static long startTime = 0;

	private AutoInvisible() {}

	public static void tick(Minecraft mc) {
		Player p = mc.player;
		if (p == null || mc.gameMode == null) return;

		if (drinking) {
			if (System.currentTimeMillis() - startTime > 1700 || !isInvisPotion(p.getMainHandItem())) {
				mc.options.keyUse.setDown(false);
				if (savedSlot >= 0) p.getInventory().setSelectedSlot(savedSlot);
				savedSlot = -1;
				drinking = false;
			}
			return;
		}

		if (p.hasEffect(MobEffects.INVISIBILITY)) return;
		if (p.getHealth() > p.getMaxHealth() * 0.5f) return;
		if (p.isUsingItem()) return;

		int slot = findInvisPotionSlot(p);
		if (slot < 0) return;

		savedSlot = p.getInventory().getSelectedSlot();
		p.getInventory().setSelectedSlot(slot);
		mc.options.keyUse.setDown(true);
		drinking = true;
		startTime = System.currentTimeMillis();
	}

	private static int findInvisPotionSlot(Player p) {
		for (int i = 0; i < 9; i++) {
			if (isInvisPotion(p.getInventory().getItem(i))) return i;
		}
		return -1;
	}

	private static boolean isInvisPotion(ItemStack s) {
		if (s.isEmpty()) return false;
		if (!s.is(Items.POTION) && !s.is(Items.SPLASH_POTION) && !s.is(Items.LINGERING_POTION)) return false;
		PotionContents pc = s.get(DataComponents.POTION_CONTENTS);
		if (pc == null) return false;
		for (var inst : pc.getAllEffects()) {
			if (inst.getEffect() == MobEffects.INVISIBILITY) return true;
		}
		return false;
	}
}
