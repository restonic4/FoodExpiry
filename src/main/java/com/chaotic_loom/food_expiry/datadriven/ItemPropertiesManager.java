package com.chaotic_loom.food_expiry.datadriven;

import com.chaotic_loom.food_expiry.FoodExpiry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

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

                                    // Parse base properties
                                    long ticks = getOrDefault(itemObject, "ticks", ItemProperties.DEFAULT.getTicks());
                                    float pristineThreshold = getOrDefault(itemObject, "pristineThreshold", ItemProperties.DEFAULT.getPristineThreshold());
                                    float freshThreshold = getOrDefault(itemObject, "freshThreshold", ItemProperties.DEFAULT.getFreshThreshold());
                                    float agingThreshold = getOrDefault(itemObject, "agingThreshold", ItemProperties.DEFAULT.getAgingThreshold());
                                    float staleThreshold = getOrDefault(itemObject, "staleThreshold", ItemProperties.DEFAULT.getStaleThreshold());
                                    float spoiledThreshold = getOrDefault(itemObject, "spoiledThreshold", ItemProperties.DEFAULT.getSpoiledThreshold());

                                    // Parse temperatureVelocityModifiers
                                    Map<String, Float> temperatureVelocityModifiers = new HashMap<>();
                                    if (itemObject.has("temperatureVelocityModifiers") && itemObject.get("temperatureVelocityModifiers").isJsonObject()) {
                                        JsonObject tempObj = itemObject.getAsJsonObject("temperatureVelocityModifiers");
                                        tempObj.entrySet().forEach(entry -> {
                                            temperatureVelocityModifiers.put(entry.getKey(), entry.getValue().getAsFloat());
                                        });
                                    } else {
                                        temperatureVelocityModifiers.put("cold", ItemProperties.DEFAULT.getTemperatureVelocityModifiers().get("cold"));
                                        temperatureVelocityModifiers.put("normal", ItemProperties.DEFAULT.getTemperatureVelocityModifiers().get("normal"));
                                        temperatureVelocityModifiers.put("hot", ItemProperties.DEFAULT.getTemperatureVelocityModifiers().get("hot"));
                                    }

                                    // Parse seasonVelocityModifiers
                                    Map<String, Float> seasonVelocityModifiers = new HashMap<>();
                                    if (itemObject.has("seasonVelocityModifiers") && itemObject.get("seasonVelocityModifiers").isJsonObject()) {
                                        JsonObject seasonObj = itemObject.getAsJsonObject("seasonVelocityModifiers");
                                        seasonObj.entrySet().forEach(entry -> {
                                            seasonVelocityModifiers.put(entry.getKey(), entry.getValue().getAsFloat());
                                        });
                                    } else {
                                        seasonVelocityModifiers.put("spring", ItemProperties.DEFAULT.getSeasonVelocityModifiers().get("spring"));
                                        seasonVelocityModifiers.put("summer", ItemProperties.DEFAULT.getSeasonVelocityModifiers().get("summer"));
                                        seasonVelocityModifiers.put("autumn", ItemProperties.DEFAULT.getSeasonVelocityModifiers().get("autumn"));
                                        seasonVelocityModifiers.put("winter", ItemProperties.DEFAULT.getSeasonVelocityModifiers().get("winter"));
                                    }

                                    // Crear la instancia de ItemProperties con los nuevos modificadores
                                    ItemProperties properties = new ItemProperties(
                                            ticks, pristineThreshold, freshThreshold, agingThreshold, staleThreshold, spoiledThreshold,
                                            temperatureVelocityModifiers, seasonVelocityModifiers
                                    );

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
