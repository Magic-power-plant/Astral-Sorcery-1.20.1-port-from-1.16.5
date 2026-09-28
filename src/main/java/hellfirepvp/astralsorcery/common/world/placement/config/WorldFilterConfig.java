package hellfirepvp.astralsorcery.common.world.placement.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.List;
import java.util.function.Supplier;

public class WorldFilterConfig {

    public static final Codec<WorldFilterConfig> CODEC = RecordCodecBuilder.create(codecInstance -> {
        return codecInstance.group(Codec.BOOL.fieldOf("ignoreFilter").forGetter(config -> {
            return config.ignoreFilter.get();
        }), ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("worldFilter").forGetter(config -> {
            return config.worldFilter.get();
        })).apply(codecInstance, WorldFilterConfig::new);
    });

    private final Supplier<Boolean> ignoreFilter;
    private final Supplier<List<ResourceKey<Level>>> worldFilter;

    public WorldFilterConfig(boolean ignoreFilter, List<ResourceKey<Level>> worldFilter) {
        this(() -> ignoreFilter, () -> worldFilter);
    }

    public WorldFilterConfig(Supplier<Boolean> ignoreFilter, Supplier<List<ResourceKey<Level>>> worldFilter) {
        this.ignoreFilter = ignoreFilter;
        this.worldFilter = worldFilter;
    }

    public boolean generatesIn(LevelAccessor world) {
        if (this.ignoreFilter.get()) {
            return true;
        }
        //During world generation this is a WorldGenRegion, which implements LevelAccessor but is *not* a Level:
        //the old unconditional (Level) cast threw a ClassCastException on the first decorated chunk.
        ResourceKey<Level> dimension;
        if (world instanceof Level level) {
            dimension = level.dimension();
        } else if (world instanceof ServerLevelAccessor serverAccessor) {
            dimension = serverAccessor.getLevel().dimension();
        } else {
            return false;
        }
        return this.worldFilter.get().contains(dimension);
    }
}
