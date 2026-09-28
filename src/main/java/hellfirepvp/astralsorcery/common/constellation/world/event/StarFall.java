package hellfirepvp.astralsorcery.common.constellation.world.event;

import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import net.minecraft.world.level.Level;

import java.util.Random;

public class StarFall extends CelestialEvent {

    @Override
    public void tick(Level world, Random rand, WorldContext ctx) {

    }

    @Override
    public boolean isActiveNow() {
        return false;
    }

    @Override
    public boolean isActiveDay() {
        return false;
    }

    @Override
    public float getEffectTick(float pTicks) {
        return 0;
    }

    @Override
    public long getSeedModifier() {
        return 0L;
    }
}
