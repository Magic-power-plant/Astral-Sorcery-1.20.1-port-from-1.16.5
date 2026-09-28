package hellfirepvp.astralsorcery.common.world.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hellfirepvp.astralsorcery.common.world.placement.config.ChanceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class ChancePlacement extends PlacementModifier {

    public static final Codec<ChancePlacement> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(ChanceConfig.CODEC.fieldOf("config").forGetter(p -> p.config))
            .apply(instance, ChancePlacement::new)
    );

    //The registry entry itself. type() has to hand back *this* instance: PlacementModifier.CODEC serialises a
    //modifier by looking its type up in BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, so a fresh lambda per call
    //("not a registered type") makes every re-encode fail.
    public static final PlacementModifierType<ChancePlacement> TYPE = () -> CODEC;

    private final ChanceConfig config;

    public ChancePlacement(ChanceConfig config) {
        this.config = config;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        return config.test(new java.util.Random(random.nextLong())) ? Stream.of(pos) : Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
