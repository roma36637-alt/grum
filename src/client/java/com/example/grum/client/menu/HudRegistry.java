package com.example.grum.client.menu;

import com.example.grum.client.GrumConfig;

import java.util.LinkedHashMap;
import java.util.Map;

/** Реестр позиций HUD. Каждый HUD-модуль через get(id, defX, defY) получает свою точку. */
public final class HudRegistry {
	private static final Map<String, int[]> bounds = new LinkedHashMap<>();

	private HudRegistry() {}

	public static int[] get(String id, int defaultX, int defaultY) {
		Map<String, int[]> map = GrumConfig.get().hudPositions;
		int[] pos = map.get(id);
		if (pos == null || pos.length < 2) {
			pos = new int[]{ defaultX, defaultY };
			map.put(id, pos);
		}
		return pos;
	}

	public static void recordBounds(String id, int x, int y, int w, int h) {
		bounds.put(id, new int[]{ x, y, w, h });
	}

	public static Map<String, int[]> getBounds() {
		return bounds;
	}

	public static void setPos(String id, int x, int y) {
		Map<String, int[]> map = GrumConfig.get().hudPositions;
		int[] pos = map.computeIfAbsent(id, k -> new int[2]);
		pos[0] = x;
		pos[1] = y;
	}
}
