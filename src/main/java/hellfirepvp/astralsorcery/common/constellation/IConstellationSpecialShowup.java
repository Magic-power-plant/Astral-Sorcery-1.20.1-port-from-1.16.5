package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.world.level.Level;

public interface IConstellationSpecialShowup extends IConstellation {
    boolean doesShowUp(Level level, long day);

    float getDistribution(Level level, long day, boolean showingUp);

    default long dayToWorldTime(long day) {
        return day * 24000L;
    }
}
