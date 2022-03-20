package tfar.meandering.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.meandering.Meandering;

@Mixin(PlayerEntity.class)
abstract class PlayerMixin extends Entity {


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z", ordinal = 1),
            method = "tick"
    )
    private void init(CallbackInfo ci) {
        if (Meandering.meanderingSideSafe((PlayerEntity) (Object) this)) {
            noClip = true;
        }
    }

    /*@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;updateCape()V"),
            method = "tick"
    )

    private void init3(CallbackInfo ci) {
        if (Meandering.meanderingSideSafe((PlayerEntity) (Object) this)) {
            noClip = false;
        }
    }*/

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSleeping()Z", ordinal = 0),
            method = "tick"
    )
    private void init2(CallbackInfo ci) {
        if (Meandering.meanderingSideSafe((PlayerEntity) (Object) this)) {
            this.onGround = false;
        }
    }


    public PlayerMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }
}
