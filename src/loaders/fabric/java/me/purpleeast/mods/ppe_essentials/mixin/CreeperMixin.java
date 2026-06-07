package me.purpleeast.mods.ppe_essentials.mixin;

import me.purpleeast.mods.ppe_essentials.PpeMobGriefing;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Creeper.class)
public class CreeperMixin {
    @ModifyArg(
            method = "explodeCreeper",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"
            ),
            index = 5,
            require = 0
    )
    @Group(name = "ppeEssential$preventCreeperBlockDamage", min = 1)
    private Level.ExplosionInteraction ppeEssential$preventBlockDamage(Level.ExplosionInteraction interaction) {
        return ppeEssential$blockDamageInteraction(interaction);
    }

    @ModifyArg(
            method = "explodeCreeper",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)V"
            ),
            index = 5,
            require = 0
    )
    @Group(name = "ppeEssential$preventCreeperBlockDamage", min = 1)
    private Level.ExplosionInteraction ppeEssential$preventBlockDamageModern(Level.ExplosionInteraction interaction) {
        return ppeEssential$blockDamageInteraction(interaction);
    }

    private Level.ExplosionInteraction ppeEssential$blockDamageInteraction(Level.ExplosionInteraction interaction) {
        if (PpeMobGriefing.shouldPreventBlockGriefing((Creeper) (Object) this)) {
            return Level.ExplosionInteraction.NONE;
        }
        return interaction;
    }
}
