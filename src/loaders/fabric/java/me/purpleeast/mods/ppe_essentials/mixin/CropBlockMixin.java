package me.purpleeast.mods.ppe_essentials.mixin;

import me.purpleeast.mods.ppe_essentials.PpeMobGriefing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CropBlock.class)
public class CropBlockMixin {
    @Redirect(
            method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean ppeEssential$preventRavagerCropBreaking(Level level, BlockPos pos, boolean drop, Entity entity) {
        if (entity instanceof Ravager && PpeMobGriefing.shouldPreventBlockGriefing(entity)) {
            return false;
        }
        return level.destroyBlock(pos, drop, entity);
    }
}
