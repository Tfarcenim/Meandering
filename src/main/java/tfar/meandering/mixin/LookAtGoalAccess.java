package tfar.meandering.mixin;

import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LookAtGoal.class)
public interface LookAtGoalAccess {
	@Accessor
	Class<?> getWatchedClass();
}
