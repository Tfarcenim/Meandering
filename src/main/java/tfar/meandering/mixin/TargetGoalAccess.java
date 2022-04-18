package tfar.meandering.mixin;

import net.minecraft.entity.ai.goal.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TargetGoal.class)
public interface TargetGoalAccess {
	@Accessor
	boolean getShouldCheckSight();

	@Accessor boolean getNearbyOnly();
}
