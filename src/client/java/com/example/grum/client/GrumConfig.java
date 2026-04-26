package com.example.grum.client;

import com.example.grum.GrumMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GrumConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("grum.json");

	public int accentColor = 0xFF55B0FF;

	// === Visuals ===
	public boolean видетьНевидимых = false;
	public boolean customHitbox = false;
	public boolean targetEsp = false;
	public boolean trapEsp = false;
	public boolean itemHighlighter = false;
	public boolean friendMarkers = false;
	public boolean killEffects = false;
	public boolean tntTimer = false;
	public boolean prediction = false;
	public boolean soundEsp = false;
	public boolean itemRadius = false;
	public boolean customFog = false;
	public double customFogDistance = 64.0;

	// === HUD ===
	public boolean armorHud = true;
	public boolean targetHud = true;
	public boolean potionsHud = true;
	public boolean effectNotify = true;
	public boolean watermarkHud = true;
	public boolean worldHud = true;
	public boolean playerHud = false;
	public boolean keybindsHud = false;
	public boolean cooldownsHud = false;
	public boolean dynamicIsland = false;
	public boolean inventoryHud = false;

	// === Combat ===
	public boolean hitSound = false;
	public boolean shiftTap = false;
	public boolean autoSwap = false;

	// === Movement / Auto ===
	public boolean autoSprint = false;
	public boolean autoEat = false;
	public int autoEatThreshold = 17;
	public boolean autoAccept = false;
	public boolean autoInvisible = false;
	public boolean elytraUtils = false;
	public boolean autoNear = false;
	public long autoNearIntervalMs = 30000;
	public boolean autoJoin = false;
	public String autoJoinCommand = "";
	public boolean middleClick = false;
	public String middleClickAction = "friend";

	// === Utility ===
	public boolean deathCoords = false;
	public boolean totemTracker = false;
	public boolean itemPickup = false;

	// === Friends list (никнеймы) ===
	public java.util.List<String> friends = new java.util.ArrayList<>();

	private static GrumConfig INSTANCE;

	public static GrumConfig get() {
		if (INSTANCE == null) INSTANCE = load();
		return INSTANCE;
	}

	private static GrumConfig load() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			if (Files.exists(CONFIG_PATH)) {
				String json = Files.readString(CONFIG_PATH);
				GrumConfig cfg = GSON.fromJson(json, GrumConfig.class);
				if (cfg == null) cfg = new GrumConfig();
				if (cfg.friends == null) cfg.friends = new java.util.ArrayList<>();
				return cfg;
			}
		} catch (Exception e) {
			GrumMod.LOGGER.warn("[grum] не удалось прочитать конфиг: {}", e.getMessage());
		}
		GrumConfig cfg = new GrumConfig();
		cfg.save();
		return cfg;
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException e) {
			GrumMod.LOGGER.warn("[grum] не удалось сохранить конфиг: {}", e.getMessage());
		}
	}
}
