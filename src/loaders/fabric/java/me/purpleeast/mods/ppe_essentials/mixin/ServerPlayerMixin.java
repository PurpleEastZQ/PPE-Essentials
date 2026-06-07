package me.purpleeast.mods.ppe_essentials.mixin;

import me.purpleeast.mods.ppe_essentials.PpeLocation;
import me.purpleeast.mods.ppe_essentials.PpePlayerData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At("HEAD"))
    private void ppeEssential$recordTeleportBack(ServerLevel level, double x, double y, double z, float yRot, float xRot, CallbackInfo callback) {
        ppeEssential$recordTeleportBack();
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z", at = @At("HEAD"))
    private void ppeEssential$recordTeleportBack(ServerLevel level, double x, double y, double z, Set<RelativeMovement> relativeMovements, float yRot, float xRot, CallbackInfoReturnable<Boolean> callback) {
        ppeEssential$recordTeleportBack();
    }

    private void ppeEssential$recordTeleportBack() {
        ServerPlayer player = (ServerPlayer) (Object) this;
        PpePlayerData.get(player.server).setTeleportBack(player.getUUID(), PpeLocation.of(player));
    }
}
