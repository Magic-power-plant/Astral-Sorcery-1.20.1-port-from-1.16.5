package hellfirepvp.astralsorcery.api;

import hellfirepvp.astralsorcery.common.blockentity.ritual.RitualPedestalBlockEntity;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** Behavior contract implemented by a constellation ritual effect. */
public interface IConstellationEffect {

    default void playClientEffect(Level world, BlockPos pos, RitualPedestalBlockEntity pedestal,
                                  float alphaMultiplier, boolean extended) {}

    boolean playEffect(Level world, BlockPos pos, ConstellationEffectProperties properties,
                       @Nullable IMinorConstellation trait);
}
