package tfar.meandering.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tfar.meandering.Meandering;

@Mixin(TargetGoal.class)
public class TargetGoalMixin {

    @Inject(method = "shouldContinueExecuting",
            at = @At(value = "INVOKE",target = "Lnet/minecraft/entity/MobEntity;setAttackTarget(Lnet/minecraft/entity/LivingEntity;)V"),cancellable = true,
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkMeander(CallbackInfoReturnable<Boolean> cir, LivingEntity livingentity, Team team, Team team1, double d0) {
        if (Meandering.isMeandering(livingentity)) {
            cir.setReturnValue(false);
        }
    }
}
