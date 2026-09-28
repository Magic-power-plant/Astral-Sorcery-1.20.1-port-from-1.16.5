package hellfirepvp.astralsorcery.common.config;

import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = hellfirepvp.astralsorcery.AstralSorcery.MODID)
public class ConfigAS {

    public static final String CATEGORY_STARLIGHT = "starlight";
    public static final String CATEGORY_ALTAR = "altar";
    public static final String CATEGORY_RENDERING = "rendering";
    public static final String CATEGORY_PERKS = "perks";
    public static final String CATEGORY_INTEGRATIONS = "integrations";

    public static int STARLIGHT_COLLECTOR_RATE = 100;
    public static int WELL_MAX_STORAGENELIGHT = 1000;
    public static float ALTAR_CRAFTING_SPEED = 1.0f;
    public static float ALTAR_STARLIGHT_BONUS = 0.0f;
    public static boolean ENABLE_STAR_RENDERING = true;
    public static boolean ENABLE_CONSTELLATION_RENDERING = true;
    public static int CONSTELLATION_VISIBILITY_START = 20;
    public static int CONSTELLATION_VISIBILITY_END = 5;
    public static float STAR_BRIGHTNESS_MULTIPLIER = 1.0f;
    public static float CONSTELLATION_BRIGHTNESS_MULTIPLIER = 1.0f;
    public static boolean ENABLE_PERK_SYSTEM = true;
    public static int PERK_EXP_GAIN_MULTIPLIER = 1;
    public static int PERK_MAX_TIER = 3;
    public static boolean ENABLE_CURIOS_INTEGRATION = true;
    public static boolean ENABLE_REI_INTEGRATION = true;
    public static boolean ENABLE_EMI_INTEGRATION = true;
    public static boolean ENABLE_BOTANIA_INTEGRATION = false;
    public static boolean ENABLE_PATCHOULI_INTEGRATION = false;

    public static int getCollectorRate() {
        return STARLIGHT_COLLECTOR_RATE;
    }

    public static int getMaxStorage() {
        return WELL_MAX_STORAGENELIGHT;
    }

    public static float getCraftingSpeed() {
        return ALTAR_CRAFTING_SPEED;
    }

    public static float getStarlightBonus() {
        return ALTAR_STARLIGHT_BONUS;
    }

    public static boolean shouldRenderStars() {
        return ENABLE_STAR_RENDERING;
    }

    public static boolean shouldRenderConstellations() {
        return ENABLE_CONSTELLATION_RENDERING;
    }

    public static boolean shouldEnablePerks() {
        return ENABLE_PERK_SYSTEM;
    }

    public static int getPerkExpMultiplier() {
        return PERK_EXP_GAIN_MULTIPLIER;
    }

    public static boolean shouldIntegrateCurios() {
        return ENABLE_CURIOS_INTEGRATION;
    }

    public static boolean shouldIntegrateREI() {
        return ENABLE_REI_INTEGRATION;
    }

    public static boolean shouldIntegrateEMI() {
        return ENABLE_EMI_INTEGRATION;
    }
}