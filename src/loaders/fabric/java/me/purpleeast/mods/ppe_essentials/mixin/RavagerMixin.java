package me.purpleeast.mods.ppe_essentials.mixin;

import me.purpleeast.mods.ppe_essentials.PpeMobGriefing;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Ravager.class)
public class RavagerMixin {
    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"
            )
    )
    private boolean ppeEssential$preventLeafBreaking(GameRules gameRules, GameRules.Key<GameRules.BooleanValue> key) {
        if (key == GameRules.RULE_MOBGRIEFING && PpeMobGriefing.shouldPreventBlockGriefing((Ravager) (Object) this)) {
            return false;
        }
        return gameRules.getBoolean(key);
    }
}
