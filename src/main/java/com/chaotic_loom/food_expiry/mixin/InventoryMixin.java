package com.chaotic_loom.food_expiry.mixin;

import com.chaotic_loom.food_expiry.datadriven.FEDataDrivenManager;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow public abstract ItemStack getItem(int i);

    @Inject(
            method = "addResource(ILnet/minecraft/world/item/ItemStack;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;grow(I)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onAddResource(
            int slot,
            ItemStack newStack,
            CallbackInfoReturnable<Integer> cir,
            @Local(ordinal = 1) ItemStack originalStack,
            @Local(ordinal = 2) int addedAmount
    ) {
        if (FEDataDrivenManager.hasTag(newStack)) {
            CompoundTag newTag = newStack.getTag();
            ListTag newTicks = newTag.getList(FEDataDrivenManager.TICKS_TAG, Tag.TAG_LONG);

            // Tomar 'addedAmount' entradas del nuevo stack
            ListTag takenEntries = new ListTag();
            for(int i = 0; i < Math.min(addedAmount, newTicks.size()); i++) {
                takenEntries.add(newTicks.get(i));
            }

            // Actualizar el nuevo stack con entradas restantes
            ListTag remainingEntries = new ListTag();
            for(int i = takenEntries.size(); i < newTicks.size(); i++) {
                remainingEntries.add(newTicks.get(i));
            }
            newTag.put(FEDataDrivenManager.TICKS_TAG, remainingEntries);
            if (remainingEntries.isEmpty()) {
                newTag.remove(FEDataDrivenManager.TICKS_TAG);
            }

            // Añadir entradas al stack original
            CompoundTag originalTag = originalStack.getOrCreateTag();
            ListTag originalTicks = originalTag.getList(FEDataDrivenManager.TICKS_TAG, Tag.TAG_LONG);
            originalTicks.addAll(takenEntries);
            originalTag.put(FEDataDrivenManager.TICKS_TAG, originalTicks);

            // Corregir conteos
            FEDataDrivenManager.fixFood(originalStack);
            FEDataDrivenManager.fixFood(newStack);
        }
    }
}
