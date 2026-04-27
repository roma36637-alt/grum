package com.triggerbot;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerBotMod implements ModInitializer {
	public static final String MOD_ID = "triggerbot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("TriggerBot common init");
	}
}
