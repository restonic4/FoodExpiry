package com.chaotic_loom.food_expiry.datadriven;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class FoodExpiryDataDrivenManager {
    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ItemPropertiesManager.getInstance());
    }
}
