package hellfirepvp.astralsorcery.client.util.word;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.Map;

public abstract class RandomWordGenerator {

    public static Map<String, RandomWordGenerator> localizedProviders = Maps.newHashMap();
    private static RandomWordGenerator fallback;

    @Nonnull
    public static RandomWordGenerator getGenerator() {
        String lang = Minecraft.getInstance().options.languageCode;
        if (lang == null) {
            return fallback;
        }
        lang = lang.toLowerCase();
        RandomWordGenerator gen;
        if ((gen = localizedProviders.get(lang)) == null) {
            gen = fallback;
        }
        return gen;
    }

    public abstract String generateWord(long seed, int length);

    //Verbatim 1.16.5 client/util/word/RandomWordGenerator#init. This was a `// TODO_STUB`, which made
    //getGenerator() return null for every language despite being @Nonnull. ConstellationGenerator:52
    //dereferences it unguarded, reached from GatewayUI#initializePlayerConstellations via
    //TileCelestialGateway's client tick whenever a player comes within 2.5 blocks of a LOCKED gateway that has
    //allowed users - so approaching one threw a ReportedException out of the block entity ticker and hard-crashed
    //the client, repeatably, including for the gateway's own owner. ClientProxy already calls init().
    public static void init() {
        fallback = new WordGeneratorEnglish();

        //Add new/other providers here...
        localizedProviders.put("en_us", fallback);
        localizedProviders.put("zh_cn", new WordGeneratorChinese());
    }
}
