package com.chaotic_loom.food_expiry.datadriven;

import com.chaotic_loom.food_expiry.FoodExpiry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class FEDataDrivenManager {
    public static final String TICKS_TAG = FoodExpiry.MOD_ID + ":ticks";

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ItemPropertiesManager.getInstance());
    }

    public static boolean hasTag(ItemStack itemStack) {
        return itemStack.hasTag() && itemStack.getTag().contains(TICKS_TAG);
    }

    public static void fixFood(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag ticks = tag.getList(FEDataDrivenManager.TICKS_TAG, Tag.TAG_LONG);
        int itemCount = stack.getCount();

        // Si la lista ya tiene el tamaño correcto, no hacemos nada
        if (ticks.size() == itemCount) {
            return;
        }

        // Ejecuta el arreglo según sea necesario
        fixFoodTicks(ticks, itemCount, stack);

        // Actualiza la lista en el tag del ItemStack
        tag.put(FEDataDrivenManager.TICKS_TAG, ticks);
    }

    public static void fixFewerFood(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag ticks = tag.getList(FEDataDrivenManager.TICKS_TAG, Tag.TAG_LONG);
        int itemCount = stack.getCount();

        // Si la lista ya tiene el tamaño correcto, no hacemos nada
        if (ticks.size() == itemCount) {
            return;
        }

        // Ejecuta el arreglo según sea necesario
        fixFewerTicks(ticks, itemCount, new Random(), stack);

        // Actualiza la lista en el tag del ItemStack
        tag.put(FEDataDrivenManager.TICKS_TAG, ticks);
    }

    private static void fixFoodTicks(ListTag ticks, int itemCount, ItemStack stack) {
        Random random = new Random();

        // Si hay menos ticks que items, se rellenan los huecos
        if (ticks.size() < itemCount) {
            fixFewerTicks(ticks, itemCount, random, stack);
        }
        // Si hay más ticks que items, se eliminan los extras
        else if (ticks.size() > itemCount) {
            fixMoreTicks(ticks, itemCount, random, stack);
        }
    }

    private static void fixFewerTicks(ListTag ticks, int itemCount, Random random, ItemStack stack) {
        FoodExpiry.LOGGER.warn("Fixing food stack, found fewer ticks than items ({} < {}) ({})",
                ticks.size(), itemCount, BuiltInRegistries.ITEM.getKey(stack.getItem()));

        // Si la lista está vacía se usa un valor por defecto (0)
        long defaultValue = 0L;
        if (!ticks.isEmpty()) {
            // Se toma un valor aleatorio de la lista existente
            defaultValue = ((LongTag) ticks.get(random.nextInt(ticks.size()))).getAsLong();
        }

        // Rellenamos hasta alcanzar el número de items
        while (ticks.size() < itemCount) {
            ticks.add(LongTag.valueOf(defaultValue));
        }
    }

    private static void fixMoreTicks(ListTag ticks, int itemCount, Random random, ItemStack stack) {
        FoodExpiry.LOGGER.warn("Fixing food stack, found more ticks than items ({} > {}) ({})",
                ticks.size(), itemCount, BuiltInRegistries.ITEM.getKey(stack.getItem()));

        // Eliminamos entradas al azar hasta que el tamaño coincida con la cantidad de items
        while (ticks.size() > itemCount) {
            int removeIndex = random.nextInt(ticks.size());
            ticks.remove(removeIndex);
        }
    }
}
