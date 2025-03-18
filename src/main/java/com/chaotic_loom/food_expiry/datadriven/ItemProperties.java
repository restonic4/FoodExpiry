package com.chaotic_loom.food_expiry.datadriven;

import java.util.Map;

public class ItemProperties {
    public static final ItemProperties DEFAULT = new ItemProperties(
            144000, 0.9f, 0.7f, 0.6f, 0.5f, 0.4f,
            getDefaultTemperatureModifiers(), getDefaultSeasonModifiers()
    );

    private long ticks;
    private float pristineThreshold;
    private float freshThreshold;
    private float agingThreshold;
    private float staleThreshold;
    private float spoiledThreshold;

    // Nuevos campos para los modificadores de velocidad
    private Map<String, Float> temperatureVelocityModifiers;
    private Map<String, Float> seasonVelocityModifiers;

    public ItemProperties(long ticks, float pristineThreshold, float freshThreshold,
                          float agingThreshold, float staleThreshold, float spoiledThreshold,
                          Map<String, Float> temperatureVelocityModifiers, Map<String, Float> seasonVelocityModifiers) {
        this.ticks = ticks;
        this.pristineThreshold = pristineThreshold;
        this.freshThreshold = freshThreshold;
        this.agingThreshold = agingThreshold;
        this.staleThreshold = staleThreshold;
        this.spoiledThreshold = spoiledThreshold;
        this.temperatureVelocityModifiers = temperatureVelocityModifiers;
        this.seasonVelocityModifiers = seasonVelocityModifiers;
    }

    // Getters
    public long getTicks() { return ticks; }
    public float getPristineThreshold() { return pristineThreshold; }
    public float getFreshThreshold() { return freshThreshold; }
    public float getAgingThreshold() { return agingThreshold; }
    public float getStaleThreshold() { return staleThreshold; }
    public float getSpoiledThreshold() { return spoiledThreshold; }

    public Map<String, Float> getTemperatureVelocityModifiers() { return temperatureVelocityModifiers; }
    public Map<String, Float> getSeasonVelocityModifiers() { return seasonVelocityModifiers; }

    public String getStatus(long ticksPassed) {
        if (ticksPassed < 0) {
            ticksPassed = 0;
        }

        float percentageLeft = 1.0f - ((float) ticksPassed / ticks);

        if (percentageLeft >= pristineThreshold) {
            return "Pristine";
        } else if (percentageLeft >= freshThreshold) {
            return "Fresh";
        } else if (percentageLeft >= agingThreshold) {
            return "Aging";
        } else if (percentageLeft >= staleThreshold) {
            return "Stale";
        } else if (percentageLeft >= spoiledThreshold) {
            return "Spoiled";
        } else {
            return "Rotten";
        }
    }

    public float getDecompositionMultiplier(String temperature, String season) {
        float tempMultiplier = temperatureVelocityModifiers.getOrDefault(temperature, DEFAULT.getTemperatureVelocityModifiers().get(temperature));
        float seasonMultiplier = seasonVelocityModifiers.getOrDefault(season, DEFAULT.getSeasonVelocityModifiers().get(season));
        return tempMultiplier * seasonMultiplier;
    }

    private static Map<String, Float> getDefaultTemperatureModifiers() {
        return Map.of(
                "cold", 1.0f,
                "normal", 1.0f,
                "hot", 1.0f
        );
    }

    private static Map<String, Float> getDefaultSeasonModifiers() {
        return Map.of(
                "spring", 1.0f,
                "summer", 1.0f,
                "autumn", 1.0f,
                "winter", 1.0f
        );
    }
}
