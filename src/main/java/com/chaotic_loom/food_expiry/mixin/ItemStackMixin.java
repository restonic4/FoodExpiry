package com.chaotic_loom.food_expiry.mixin;

import com.chaotic_loom.food_expiry.FoodExpiry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        if (!nbt.contains(FoodExpiry.MOD_ID + ":ticks")) {
            ListTag timestamps = new ListTag();
            for (int i = 0; i < stack.getCount(); i++) {
                timestamps.add(LongTag.valueOf(0));
            }
            nbt.put(FoodExpiry.MOD_ID + ":ticks", timestamps);
        }
    }

    @Inject(method = "split", at = @At("RETURN"))
    private void onSplit(int size, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack original = (ItemStack) (Object) this;
        ItemStack newStack = cir.getReturnValue();

        if (original.hasTag() && original.getTag().contains(FoodExpiry.MOD_ID + ":ticks")) {
            ListTag originalTimestamps = original.getTag().getList(FoodExpiry.MOD_ID + ":ticks", Tag.TAG_LONG);
            int originalNewCount = original.getCount();

            ListTag newTicks = new ListTag();
            for (int i = originalNewCount; i < originalTimestamps.size(); i++) {
                newTicks.add(originalTimestamps.get(i));
            }

            ListTag remainingTicks = new ListTag();
            for (int i = 0; i < originalNewCount; i++) {
                remainingTicks.add(originalTimestamps.get(i));
            }

            original.getTag().put(FoodExpiry.MOD_ID + ":ticks", remainingTicks);
            newStack.getOrCreateTag().put(FoodExpiry.MOD_ID + ":ticks", newTicks);
        }
    }

    @Inject(method = "grow", at = @At("HEAD"))
    private void add(int i, CallbackInfo ci) {
        // TODO: Look at Inventory.addResource() its where combining takes place
    }
}
