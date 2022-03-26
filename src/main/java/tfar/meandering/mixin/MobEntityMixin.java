package tfar.meandering.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfar.meandering.Meandering;

@Mixin(MobEntity.class)
public class MobEntityMixin {

    @Inject(method = "canAttack(Lnet/minecraft/entity/LivingEntity;)Z",at = @At("HEAD"),cancellable = true)
    private void checkMeander(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (Meandering.isMeandering(target)) {
            cir.setReturnValue(false);
        }
    }
}
