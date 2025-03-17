package com.chaotic_loom.food_expiry.mixin;

import com.chaotic_loom.food_expiry.FoodExpiry;
import com.chaotic_loom.food_expiry.datadriven.FoodExpiryDataDrivenManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
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
        if (!nbt.contains(FoodExpiryDataDrivenManager.TICKS_TAG)) {
            ListTag timestamps = new ListTag();
            for (int i = 0; i < stack.getCount(); i++) {
                timestamps.add(LongTag.valueOf(0));
            }
            nbt.put(FoodExpiryDataDrivenManager.TICKS_TAG, timestamps);
        }
    }

    @Inject(method = "split", at = @At("RETURN"))
    private void onSplit(int size, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack original = (ItemStack) (Object) this;
        ItemStack newStack = cir.getReturnValue();

        if (original.hasTag() && original.getTag().contains(FoodExpiryDataDrivenManager.TICKS_TAG)) {
            ListTag originalTimestamps = original.getTag().getList(FoodExpiryDataDrivenManager.TICKS_TAG, Tag.TAG_LONG);

            int originalCount = original.getCount();
            int splitCount = newStack.getCount();

            // Ensure the list size matches the item count before proceeding
            if (originalTimestamps.size() != originalCount + splitCount) {
                throw new IllegalStateException("Mismatch between timestamps size and item stack counts! Original ticks count: " + originalTimestamps.size() + ", original count: " + originalCount + ", new count: " + splitCount);
            }

            // Create separate timestamp lists for the split stacks
            ListTag newTicks = new ListTag();
            for (int i = 0; i < splitCount; i++) {
                if (i < originalTimestamps.size()) { // Ensure we don't exceed bounds
                    newTicks.add(originalTimestamps.get(i));
                }
            }

            ListTag remainingTicks = new ListTag();
            for (int i = splitCount; i < originalTimestamps.size(); i++) {
                remainingTicks.add(originalTimestamps.get(i));
            }

            // Set the tags for the "original" and "newStack"
            original.getTag().put(FoodExpiryDataDrivenManager.TICKS_TAG, remainingTicks);
            newStack.getOrCreateTag().put(FoodExpiryDataDrivenManager.TICKS_TAG, newTicks);
        }
    }

    // Allow stacking with different nbt data

    @Inject(method = "isSameItemSameTags", at = @At("RETURN"), cancellable = true)
    private static void isSame(ItemStack itemStack, ItemStack itemStack2, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue() && itemStack.getItem().equals(itemStack2.getItem()) && itemStack.isEdible() && itemStack2.isEdible()) {
            if (itemStack.hasTag() && itemStack.getTag().contains(FoodExpiryDataDrivenManager.TICKS_TAG) && itemStack2.hasTag() && itemStack2.getTag().contains(FoodExpiryDataDrivenManager.TICKS_TAG)) {
                cir.setReturnValue(true);
            }
        }
    }

    // Constructor

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;I)V>", at = @At("TAIL"))
    private void onConstructorInit(ItemLike itemLike, int i, CallbackInfo ci) {
        System.out.println("Se creó un ItemStack con el ítem: " + itemLike + " y la cantidad: " + i);

        // Aquí puedes agregar tu lógica personalizada
        // Ejemplo: Modificar la cantidad si un item específico se está utilizando
        if (itemLike.asItem().equals(Items.COOKED_BEEF)) {
            System.out.println("Es CARNE AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA!");
        }
    }
}
