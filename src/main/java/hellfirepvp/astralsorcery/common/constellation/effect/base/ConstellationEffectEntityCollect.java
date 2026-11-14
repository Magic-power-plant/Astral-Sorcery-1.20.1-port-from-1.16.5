package hellfirepvp.astralsorcery.common.constellation.effect.base;

import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.block.util.ILocatable;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public abstract class ConstellationEffectEntityCollect<T extends Entity> extends ConstellationEffect {

    private final Class<T> entityClazz;
    private final Predicate<T> filter;

    protected ConstellationEffectEntityCollect(@Nonnull ILocatable origin, @Nonnull IWeakConstellation cst, Class<T> entityClazz, Predicate<T> filter) {
        super(origin, cst);
        this.filter = filter;
        this.entityClazz = entityClazz;
    }

    @Nonnull
    protected List<T> collectEntities(Level world, BlockPos center, ConstellationEffectProperties properties) {
        return world.getEntitiesOfClass(this.entityClazz, BOX.move(center.getX(), center.getY(), center.getZ()).inflate(properties.getSize()), this.filter);
    }
}
