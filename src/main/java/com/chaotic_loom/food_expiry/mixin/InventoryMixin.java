package com.chaotic_loom.food_expiry.mixin;

import com.chaotic_loom.food_expiry.datadriven.FoodExpiryDataDrivenManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private void onAddResource(int slot, ItemStack newStack, CallbackInfoReturnable<Integer> cir) {
        ItemStack existingStack = this.getItem(slot);

        CompoundTag existingTag = existingStack.getOrCreateTag();
        CompoundTag newItemTag = newStack.getOrCreateTag();

        String key = FoodExpiryDataDrivenManager.TICKS_TAG;

        if (newItemTag.contains(key, Tag.TAG_LIST)) {
            ListTag combinedList = new ListTag();

            if (existingTag.contains(key, Tag.TAG_LIST)) {
                combinedList.addAll(existingTag.getList(key, Tag.TAG_LONG));
            } else {
                for (int i = 0; i < existingStack.getCount(); i++) {
                    combinedList.add(LongTag.valueOf(0));
                }
            }

            combinedList.addAll(newItemTag.getList(key, Tag.TAG_LONG));
            existingTag.put(key, combinedList);
        }
    }
}
