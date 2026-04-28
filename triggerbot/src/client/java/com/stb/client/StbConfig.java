package com.stb.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Config store. Hidden: saved to .minecraft/config/.opts.dat (no obvious name). */
public final class StbConfig {
	private static final Gson G = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance()
			.getConfigDir().resolve(".opts.dat");

	private static StbConfig INSTANCE;

	// --- trigger fields ---
	public boolean enabled = false;

	/** min delay between swings (ms) */
	public int intervalMs = 70;
	/** random jitter added on top (0..jitter) */
	public int jitterMs = 55;
	/** delay after switching target (ms) to simulate human re-aim */
	public int switchDelayMs = 120;
	/** reach distance; capped to 3.0 vanilla */
	public double reach = 3.0;
	/** wait for attack cooldown scale == 1.0 before hitting (max damage) */
	public boolean waitCooldown = true;
	/** only hit while mid-air/falling (crit) */
	public boolean critOnly = false;
	/** require sword/axe/mace/trident in main hand */
	public boolean requireWeapon = false;
	/** require line of sight to target */
	public boolean requireLos = true;
	/** max yaw delta between crosshair and target center (degrees) */
	public double maxAngle = 60.0;
	/** optional: don't attack while using an item (blocking, eating) */
	public boolean notWhileUsingItem = true;
	/** only attack while jump (Space) is held — useful for manual crit timing */
	public boolean spaceOnly = false;

	/** Target filters. */
	public boolean targetHostile = true;
	public boolean targetPassive = false;
	public boolean targetPlayers = false;
	public boolean ignoreUtility = true;

	/** random skip rate 0..1 — occasionally skip an attack to humanize */
	public double randomSkip = 0.07;

	/** Once toggled true at runtime via panic key, the bot is fully unhooked
	 * for the remainder of the session. Persisted so future sessions stay off. */
	public transient boolean unhooked = false;

	public static StbConfig get() {
		if (INSTANCE == null) INSTANCE = load();
		return INSTANCE;
	}

	private static StbConfig load() {
		try {
			if (Files.exists(PATH)) {
				String s = Files.readString(PATH);
				StbConfig c = G.fromJson(s, StbConfig.class);
				if (c != null) return c;
			}
		} catch (IOException | RuntimeException ignored) {
		}
		StbConfig c = new StbConfig();
		c.save();
		return c;
	}

	public void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, G.toJson(this));
		} catch (IOException ignored) {
		}
	}
}
