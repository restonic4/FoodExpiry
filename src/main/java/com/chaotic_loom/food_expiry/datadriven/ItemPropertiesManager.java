package com.chaotic_loom.food_expiry.datadriven;

import com.chaotic_loom.food_expiry.FoodExpiry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ItemPropertiesManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private Map<ResourceLocation, ItemProperties> itemProperties = new HashMap<>();

    private static ItemPropertiesManager INSTANCE;

    public static ItemPropertiesManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemPropertiesManager();
        }

        return INSTANCE;
    }

    private ItemPropertiesManager() {
        super(GSON, "item_properties");
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(FoodExpiry.MOD_ID, "item_properties");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceMap, ResourceManager manager, ProfilerFiller profiler) {
        FoodExpiry.LOGGER.info("Loading expiry properties...");

        Map<ResourceLocation, ItemProperties> newProperties = new HashMap<>();

        resourceMap.forEach((identifier, jsonElement) -> {
            try {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    if (jsonObject.has("items") && jsonObject.get("items").isJsonArray()) {
                        jsonObject.getAsJsonArray("items").forEach(itemElement -> {
                            if (itemElement.isJsonObject()) {
                                JsonObject itemObject = itemElement.getAsJsonObject();

                                if (itemObject.has("id") && itemObject.get("id").isJsonPrimitive()) {
                                    ResourceLocation itemId = new ResourceLocation(itemObject.get("id").getAsString());

                                    // Parse item properties
                                    long ticks = getOrDefault(itemObject, "ticks", 0L);
                                    float pristineThreshold = getOrDefault(itemObject, "pristineThreshold", 0.0f);
                                    float freshThreshold = getOrDefault(itemObject, "freshThreshold", 0.0f);
                                    float agingThreshold = getOrDefault(itemObject, "agingThreshold", 0.0f);
                                    float staleThreshold = getOrDefault(itemObject, "staleThreshold", 0.0f);
                                    float spoiledThreshold = getOrDefault(itemObject, "spoiledThreshold", 0.0f);
                                    float rottenThreshold = getOrDefault(itemObject, "rottenThreshold", 0.0f);
                                    float moldyThreshold = getOrDefault(itemObject, "moldyThreshold", 0.0f);

                                    ItemProperties properties = new ItemProperties(ticks, pristineThreshold,
                                            freshThreshold, agingThreshold, staleThreshold,
                                            spoiledThreshold, rottenThreshold, moldyThreshold);

                                    newProperties.put(itemId, properties);
                                    FoodExpiry.LOGGER.info("Loaded expiry property for: {}", itemId);
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                FoodExpiry.LOGGER.error("Error loading expiry property for: " + identifier, e);
            }
        });

        this.itemProperties = newProperties;
        FoodExpiry.LOGGER.info("Expiry properties loaded for {} items", this.itemProperties.size());
    }

    private long getOrDefault(JsonObject obj, String key, long defaultValue) {
        return obj.has(key) && obj.get(key).isJsonPrimitive() ? obj.get(key).getAsLong() : defaultValue;
    }

    private float getOrDefault(JsonObject obj, String key, float defaultValue) {
        return obj.has(key) && obj.get(key).isJsonPrimitive() ? obj.get(key).getAsFloat() : defaultValue;
    }

    public ItemProperties getPropertiesForItem(ResourceLocation itemId) {
        return itemProperties.getOrDefault(itemId, ItemProperties.DEFAULT);
    }

    public ItemProperties getPropertiesForItem(Item item) {
        return itemProperties.getOrDefault(BuiltInRegistries.ITEM.getKey(item), ItemProperties.DEFAULT);
    }

    public boolean hasPropertiesForItem(ResourceLocation itemId) {
        return itemProperties.containsKey(itemId);
    }
}