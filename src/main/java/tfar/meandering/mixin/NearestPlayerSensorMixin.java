package tfar.meandering.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.NearestPlayersSensor;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfar.meandering.Meandering;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(NearestPlayersSensor.class)
abstract class NearestPlayerSensorMixin extends Sensor<LivingEntity> {

  /*  @Inject(method = "update",at = @At("RETURN"))
    private void idleUpdate(ServerWorld worldIn, LivingEntity entityIn, CallbackInfo ci) {
        List<Entity> list = worldIn.getEntities(Meandering.IDLE_PLAYER, a -> true).stream().filter(EntityPredicates.NOT_SPECTATING).filter((player) -> {
            return entityIn.isEntityInRange(player, 16.0D);
        }).sorted(Comparator.comparingDouble(entityIn::getDistanceSq)).collect(Collectors.toList());
        Brain<?> brain = entityIn.getBrain();
        brain.setMemory(MemoryModuleType.NEAREST_PLAYERS, list);
        List<Entity> list1 = list.stream().filter((player) -> {
            return Sensor.canAttackTarget(entityIn, player);
        }).collect(Collectors.toList());
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, list1.isEmpty() ? null : list1.get(0));
        Optional<PlayerEntity> optional = list1.stream().filter(EntityPredicates.CAN_HOSTILE_AI_TARGET).findFirst();
        brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, optional);
    }*/
}
