package hellfirepvp.astralsorcery.common.effect.visual.spec;

import net.minecraft.resources.ResourceLocation;

/** Stable data identifiers. These constants do not initialize client render contexts. */
public final class EffectTemplateIds {
    public static final ResourceLocation GENERIC_PARTICLE = id("generic_particle");
    public static final ResourceLocation GENERIC_DEPTH_PARTICLE = id("generic_depth_particle");
    public static final ResourceLocation GENERIC_GATEWAY_PARTICLE = id("generic_gateway_particle");
    public static final ResourceLocation CRYSTAL_BURST_1 = id("crystal_burst_1");
    public static final ResourceLocation CRYSTAL_BURST_2 = id("crystal_burst_2");
    public static final ResourceLocation CRYSTAL_BURST_3 = id("crystal_burst_3");
    public static final ResourceLocation GEM_CRYSTAL_BURST = id("gem_crystal_burst");
    public static final ResourceLocation GEM_CRYSTAL_BURST_SKY = id("gem_crystal_burst_sky");
    public static final ResourceLocation GEM_CRYSTAL_BURST_DAY = id("gem_crystal_burst_day");
    public static final ResourceLocation GEM_CRYSTAL_BURST_NIGHT = id("gem_crystal_burst_night");
    public static final ResourceLocation COLLECTOR_BURST = id("collector_burst");
    public static final ResourceLocation LIGHTBEAM = id("lightbeam");
    public static final ResourceLocation LIGHTBEAM_TRANSFER = id("lightbeam_transfer");

    private EffectTemplateIds() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("astralsorcery", path);
    }
}
