package net.totobirdcreations.bettertotems.mixin;

import net.minecraft.advancement.criterion.LevitationCriterion;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.totobirdcreations.bettertotems.util.mixin.ILivingEntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LevitationCriterion.class)
public class LevitationCriterionMixin {

    @Inject(method = "trigger", at = @At("HEAD"), cancellable = true)
    private void trigger(ServerPlayerEntity player, Vec3d startPos, int duration, CallbackInfo ci) {
        if (((ILivingEntityMixin) player).hasVoidLevitation()) {
            ci.cancel();
        }
    }

}
