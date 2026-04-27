package com.triggerbot.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TriggerBotConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("triggerbot.json");

	public boolean enabled = false;

	/** Minimum delay between attacks in milliseconds. */
	public int delayMs = 80;

	/** Random jitter added to delay (0..jitterMs) for natural timing. */
	public int jitterMs = 40;

	/** Maximum reach distance (blocks). Vanilla survival = 3.0. */
	public double reach = 3.0;

	/** Only attack when vanilla attack cooldown is fully charged (max damage). */
	public boolean waitForCooldown = true;

	/** Only attack while falling (crit hits). */
	public boolean critOnly = false;

	/** Require a weapon (sword / axe / trident) in main hand. */
	public boolean requireWeapon = false;

	/** Entity filters. */
	public boolean targetHostile = true;
	public boolean targetPassive = false;
	public boolean targetPlayers = false;
	public boolean targetAny = false;

	/** Ignore armor stands / item frames. */
	public boolean ignoreUtilityEntities = true;

	/** Also attack when another player is not holding attack (singleplayer helper). */
	public boolean attackInSingleplayerOnly = false;

	/** Show on-screen HUD indicator. */
	public boolean showHud = true;

	/** Extra hitbox expansion when raycasting (blocks). Makes tracking easier. */
	public double hitboxExpand = 0.0;

	public static TriggerBotConfig load() {
		try {
			if (Files.exists(CONFIG_PATH)) {
				String json = Files.readString(CONFIG_PATH);
				TriggerBotConfig cfg = GSON.fromJson(json, TriggerBotConfig.class);
				if (cfg != null) return cfg;
			}
		} catch (IOException | RuntimeException e) {
			// fall through to defaults
		}
		TriggerBotConfig cfg = new TriggerBotConfig();
		cfg.save();
		return cfg;
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this));
		} catch (IOException ignored) {
		}
	}
}
