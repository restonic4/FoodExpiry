package com.chaotic_loom.food_expiry.datadriven;

public class ItemProperties {
    public static final ItemProperties DEFAULT = new ItemProperties(
            144000, 0.9f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.0f
    );

    private long ticks;
    private float pristineThreshold;
    private float freshThreshold;
    private float agingThreshold;
    private float staleThreshold;
    private float spoiledThreshold;
    private float rottenThreshold;
    private float moldyThreshold;

    public ItemProperties(long ticks, float pristineThreshold, float freshThreshold,
                          float agingThreshold, float staleThreshold, float spoiledThreshold,
                          float rottenThreshold, float moldyThreshold) {
        this.ticks = ticks;
        this.pristineThreshold = pristineThreshold;
        this.freshThreshold = freshThreshold;
        this.agingThreshold = agingThreshold;
        this.staleThreshold = staleThreshold;
        this.spoiledThreshold = spoiledThreshold;
        this.rottenThreshold = rottenThreshold;
        this.moldyThreshold = moldyThreshold;
    }

    // Getters
    public long getTicks() { return ticks; }
    public float getPristineThreshold() { return pristineThreshold; }
    public float getFreshThreshold() { return freshThreshold; }
    public float getAgingThreshold() { return agingThreshold; }
    public float getStaleThreshold() { return staleThreshold; }
    public float getSpoiledThreshold() { return spoiledThreshold; }
    public float getRottenThreshold() { return rottenThreshold; }
    public float getMoldyThreshold() { return moldyThreshold; }

    public String getStatus(long ticksPassed) {
        if (ticksPassed < 0) {
            ticksPassed = 0;
        }

        float porcentajeVidaRestante = 1.0f - ((float) ticksPassed / ticks);

        if (porcentajeVidaRestante >= pristineThreshold) {
            return "Prístino";
        } else if (porcentajeVidaRestante >= freshThreshold) {
            return "Fresco";
        } else if (porcentajeVidaRestante >= agingThreshold) {
            return "Envejeciendo";
        } else if (porcentajeVidaRestante >= staleThreshold) {
            return "Rancio";
        } else if (porcentajeVidaRestante >= spoiledThreshold) {
            return "Echado a perder";
        } else if (porcentajeVidaRestante >= rottenThreshold) {
            return "Podrido";
        } else if (porcentajeVidaRestante >= moldyThreshold) {
            return "Mohoso";
        } else {
            return "Desconocido";
        }
    }
}
