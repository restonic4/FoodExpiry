package com.chaotic_loom.food_expiry;

import com.chaotic_loom.food_expiry.datadriven.FEDataDrivenManager;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FoodExpiry implements ModInitializer {
    public static final String MOD_ID = "food_expiry";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        FEDataDrivenManager.register();
    }
}
