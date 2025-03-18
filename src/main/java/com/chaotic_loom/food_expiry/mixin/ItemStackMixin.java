package com.chaotic_loom.food_expiry.mixin;

import com.chaotic_loom.food_expiry.datadriven.FEDataDrivenManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    private void onCraftedBy(Level level, Player player, int i, CallbackInfo ci) {
        ItemStack itemStack = (ItemStack) (Object) this;

        if (!level.isClientSide() && itemStack.getItem().isEdible()) {
            initFoodData(itemStack);
        }
    }

    @Unique
    private void initFoodData(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        if (!nbt.contains(FEDataDrivenManager.TICKS_TAG)) {
            ListTag timestamps = new ListTag();
            for (int i = 0; i < stack.getCount(); i++) {
                timestamps.add(LongTag.valueOf(new Random().nextInt(1000)));
            }
            nbt.put(FEDataDrivenManager.TICKS_TAG, timestamps);
        }
    }

    @Inject(method = "split", at = @At("RETURN"))
    private void onSplit(int size, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack original = (ItemStack) (Object) this;
        ItemStack newStack = cir.getReturnValue();

        // This happens when you move stacks on you inventory / picking them with your cursor
        if (original.getCount() == 0) {
            return;
        }

        System.out.println("Split size: " + size);
        System.out.println("Original size: " + original.getCount());
        System.out.println("New size: " + newStack.getCount());

        if (FEDataDrivenManager.hasTag(original)) {
            CompoundTag originalTag = original.getTag();
            ListTag originalTicks = originalTag.getList(FEDataDrivenManager.TICKS_TAG, Tag.TAG_LONG);

            int originalCountAfterSplit = original.getCount();
            int newCount = newStack.getCount();

            ListTag originalTicksNew = new ListTag();
            ListTag newTicksNew = new ListTag();

            // Asignar entradas al stack original
            int originalTake = Math.min(originalTicks.size(), originalCountAfterSplit);
            for (int i = 0; i < originalTake; i++) {
                originalTicksNew.add(originalTicks.get(i));
            }

            // Asignar entradas al nuevo stack
            int remaining = originalTicks.size() - originalTake;
            int newTake = Math.min(remaining, newCount);
            for (int i = originalTake; i < originalTake + newTake; i++) {
                newTicksNew.add(originalTicks.get(i));
            }

            // Aplicar los nuevos tags
            originalTag.put(FEDataDrivenManager.TICKS_TAG, originalTicksNew);
            newStack.getOrCreateTag().put(FEDataDrivenManager.TICKS_TAG, newTicksNew);

            // Corregir posibles discrepancias
            FEDataDrivenManager.fixFood(original);
            FEDataDrivenManager.fixFood(newStack);
        }
    }


    // Allow stacking with different nbt data

    @Inject(method = "isSameItemSameTags", at = @At("RETURN"), cancellable = true)
    private static void isSame(ItemStack itemStack, ItemStack itemStack2, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && itemStack.getItem().equals(itemStack2.getItem()) && itemStack.isEdible() && itemStack2.isEdible()) {
            if (itemStack.hasTag() && itemStack.getTag().contains(FEDataDrivenManager.TICKS_TAG) && itemStack2.hasTag() && itemStack2.getTag().contains(FEDataDrivenManager.TICKS_TAG)) {
                cir.setReturnValue(true);
            }
        }
    }

    // Constructor

    /*@Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;I)V>", at = @At("TAIL"))
    private void onConstructorInit(ItemLike itemLike, int i, CallbackInfo ci) {
        System.out.println("Se creó un ItemStack con el ítem: " + itemLike + " y la cantidad: " + i);

        // Aquí puedes agregar tu lógica personalizada
        // Ejemplo: Modificar la cantidad si un item específico se está utilizando
        if (itemLike.asItem().equals(Items.COOKED_BEEF)) {
            System.out.println("Es CARNE AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA!");
        }
    }*/
}
