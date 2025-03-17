package com.chaotic_loom.food_expiry.datadriven;

import com.chaotic_loom.food_expiry.FoodExpiry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class FoodExpiryDataDrivenManager {
    public static final String TICKS_TAG = FoodExpiry.MOD_ID + ":ticks";

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ItemPropertiesManager.getInstance());
    }
}
