package hellfirepvp.astralsorcery.common.constellation.world;

import hellfirepvp.astralsorcery.common.data.config.entry.GeneralConfig;
import net.minecraft.world.level.Level;

public class DayTimeHelper {

    public static float getCurrentDaytimeDistribution(Level world) {
        int dLength = GeneralConfig.CONFIG.dayLength.get();
        float dayPart = ((world.getDayTime() % dLength) + dLength) % dLength;
        if (dayPart < (dLength / 2F)) return 0F;
        float part = dLength / 7F;
        if (dayPart < ((dLength / 2F) + part)) return ((dayPart - ((dLength / 2F) + part)) / part) + 1F;
        if (dayPart > (dLength - part)) return 1F - (dayPart - (dLength - part)) / part;
        return 1F;
    }

    public static boolean isNight(Level world) {
        return getCurrentDaytimeDistribution(world) >= 0.55;
    }

    public static boolean isDay(Level world) {
        return getCurrentDaytimeDistribution(world) <= 0.05;
    }
}
