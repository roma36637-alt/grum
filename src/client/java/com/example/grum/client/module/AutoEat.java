package com.example.grum.client.module;

import com.example.grum.client.GrumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

/** Автоматически ест еду из хотбара когда уровень голода падает ниже порога. */
public final class AutoEat {
	private static int savedSlot = -1;
	private static boolean eating = false;

	private AutoEat() {}

	public static void tick(Minecraft mc) {
		Player p = mc.player;
		if (p == null || mc.gameMode == null) return;
		GrumConfig cfg = GrumConfig.get();
		int hunger = p.getFoodData().getFoodLevel();

		if (!eating) {
			if (hunger > cfg.autoEatThreshold) return;
			int slot = findFoodSlot(p);
			if (slot < 0) return;
			savedSlot = p.getInventory().getSelectedSlot();
			p.getInventory().setSelectedSlot(slot);
			mc.options.keyUse.setDown(true);
			eating = true;
			return;
		}

		if (hunger >= 20 || !isFood(p.getMainHandItem())) {
			mc.options.keyUse.setDown(false);
			if (savedSlot >= 0) p.getInventory().setSelectedSlot(savedSlot);
			savedSlot = -1;
			eating = false;
		}
	}

	private static int findFoodSlot(Player p) {
		for (int i = 0; i < 9; i++) {
			ItemStack s = p.getInventory().getItem(i);
			if (isFood(s)) return i;
		}
		return -1;
	}

	private static boolean isFood(ItemStack s) {
		if (s.isEmpty()) return false;
		FoodProperties food = s.get(DataComponents.FOOD);
		return food != null;
	}
}
