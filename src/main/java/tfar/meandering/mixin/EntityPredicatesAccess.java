package tfar.meandering.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityPredicates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(EntityPredicates.class)
public interface EntityPredicatesAccess {

    @Accessor @Mutable static void setCAN_AI_TARGET(Predicate<Entity> p) {
        throw new RuntimeException();
    }

    @Accessor @Mutable static void setCAN_HOSTILE_AI_TARGET(Predicate<Entity> p) {
        throw new RuntimeException();
    }
}
