package hellfirepvp.astralsorcery.common.world.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.world.FeatureGenerationConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

//1.16.5 built this placement in code and handed it CFG_*.worldFilterConfig(), a WorldFilterConfig whose two
//values are *suppliers* reading the live ForgeConfigSpec (common/world/FeatureGenerationConfig.java:98-105).
//In 1.20.1 the placement comes out of a placed_feature json instead, so an inlined {ignoreFilter, worldFilter}
//pair would freeze whatever the config said when the json was written and the "everyWorld"/"worldNames"
//options would stop doing anything. The json therefore names the config section
//("config_entry": "rock_crystal") and the live FeatureGenerationConfig is resolved on every placement, which
//keeps 1.16.5's behaviour: editing astralsorcery-common.toml changes worldgen without touching data.
public class WorldFilteredPlacement extends PlacementModifier {

    public static final Codec<WorldFilteredPlacement> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(Codec.STRING.fieldOf("config_entry").forGetter(p -> p.configEntry))
            .apply(instance, WorldFilteredPlacement::new)
    );

    //See ChancePlacement.TYPE: the registered instance has to be the one type() returns.
    public static final PlacementModifierType<WorldFilteredPlacement> TYPE = () -> CODEC;

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private final String configEntry;

    public WorldFilteredPlacement(String configEntry) {
        this.configEntry = configEntry;
    }

    public String getConfigEntry() {
        return this.configEntry;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        FeatureGenerationConfig config = FeatureGenerationConfig.findByName(this.configEntry);
        if (config == null || !config.isConfigured()) {
            //Fail open rather than silently killing every AS feature: a typo in a datapack should be visible in
            //the log, not as an empty world.
            if (WARNED.add(this.configEntry)) {
                AstralSorcery.LOG.warn("Unknown worldgen config entry '{}' referenced by an astralsorcery:world_filter "
                        + "placement; the dimension filter for it is skipped.", this.configEntry);
            }
            return Stream.of(pos);
        }
        //1.16.5 tested "enabled" in RegistryWorldGeneration#loadBiomeFeatures, i.e. once per world load. It is
        //tested here as well so that flipping the option takes effect on the next chunk instead of the next launch.
        if (!config.isEnabled()) {
            return Stream.of();
        }
        if (config.worldFilterConfig().generatesIn(context.getLevel())) {
            return Stream.of(pos);
        }
        return Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
