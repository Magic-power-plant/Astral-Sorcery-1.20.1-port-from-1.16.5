package hellfirepvp.astralsorcery.common.block.base.template;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.FlowerBlock;

import javax.annotation.Nonnull;

/**
 * Port of 1.16.5 {@code BlockFlowerTemplate}: a flower whose suspicious-stew effect is provided by the subclass.
 */
public abstract class BlockFlowerTemplate extends FlowerBlock implements CustomItemBlock {

    public BlockFlowerTemplate(Properties properties) {
        super(() -> MobEffects.HEAL, 0, properties);
    }

    @Override
    @Nonnull
    public abstract MobEffect getSuspiciousEffect();

    @Override
    public abstract int getEffectDuration();
}
