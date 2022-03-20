package tfar.meandering.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.meandering.Meandering;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerMixin extends PlayerEntity {


    public ClientPlayerMixin(World p_i241920_1_, BlockPos p_i241920_2_, float p_i241920_3_, GameProfile p_i241920_4_) {
        super(p_i241920_1_, p_i241920_2_, p_i241920_3_, p_i241920_4_);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/player/ClientPlayerEntity;sendPlayerAbilities()V",ordinal = 1),
            method = "livingTick")
    private void init(CallbackInfo ci) {
        if (Meandering.meanderingSideSafe(this)) {
            this.abilities.isFlying = true;
        }
    }
}
