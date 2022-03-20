package tfar.meandering.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.meandering.Meandering;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerMixin extends Entity {


    @Inject(at = @At(value = "RETURN"),
            method = "updatePotionMetadata"
    )
    private void init(CallbackInfo ci) {
        if (Meandering.meanderingSideSafe((PlayerEntity) (Object) this)) {
            this.setInvisible(true);
        }
    }


    public ServerPlayerMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }
}
