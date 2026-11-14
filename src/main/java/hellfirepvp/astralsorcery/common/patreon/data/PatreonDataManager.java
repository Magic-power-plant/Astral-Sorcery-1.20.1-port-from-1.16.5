/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.patreon.data;
import com.google.gson.Gson;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectDefinition;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectHelper;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectType;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Downloads data only. Client listeners and textures are installed on the client game thread. */
public final class PatreonDataManager {
    private static final String PATREON_EFFECT_URL = "http://hellfiredev.net/patreon.json";
    private static final Gson GSON = new Gson();
    private PatreonDataManager() {}

    public static void loadPatreonEffects() {
        Thread loader = new Thread(() -> {
            try {
                URLConnection connection = new URL(PATREON_EFFECT_URL).openConnection();
                connection.setConnectTimeout(10_000);
                connection.setReadTimeout(10_000);
                try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    PatreonEffectHelper.replaceEffects(decode(reader));
                }
                AstralSorcery.LOG.info("Patreon effect loading finished.");
            } catch (Exception exception) {
                AstralSorcery.LOG.warn("Could not load patreon data; keeping the previous catalog", exception);
            }
        }, "AstralSorcery Patreon Effect Loader");
        loader.setDaemon(true);
        loader.start();
    }

    public static List<PatreonEffectDefinition> decode(Reader reader) {
        PatreonData data = GSON.fromJson(reader, PatreonData.class);
        if (data == null) throw new IllegalArgumentException("Missing patreon catalog");
        List<PatreonEffectDefinition> effects = new ArrayList<>();
        for (PatreonData.EffectEntry entry : data.getEffectList()) {
            try {
                effects.add(PatreonEffectType.valueOf(entry.getEffectClass()).getProvider()
                        .buildEffect(UUID.fromString(entry.getUuid()), entry.getParameters()));
            } catch (Exception exception) {
                AstralSorcery.LOG.warn("Skipping malformed patreon entry for {}", entry.getUuid());
            }
        }
        return List.copyOf(effects);
    }
}
