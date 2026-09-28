package hellfirepvp.astralsorcery.common.constellation.world.event;

import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.world.level.Level;

import java.util.Random;

public abstract class CelestialEvent {

    public abstract void tick(Level world, Random rand, WorldContext ctx);

    public abstract boolean isActiveNow();

    public abstract boolean isActiveDay();

    public abstract float getEffectTick(float pTicks);

    public abstract long getSeedModifier();

}
