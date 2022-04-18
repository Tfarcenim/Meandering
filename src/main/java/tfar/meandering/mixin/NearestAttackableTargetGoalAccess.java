package tfar.meandering.mixin;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.util.EntityPredicates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NearestAttackableTargetGoal.class)
public interface NearestAttackableTargetGoalAccess extends TargetGoalAccess {
	@Accessor
	Class<?> getTargetClass();

	@Accessor int getTargetChance();

	@Accessor
	EntityPredicate getTargetEntitySelector();
}
