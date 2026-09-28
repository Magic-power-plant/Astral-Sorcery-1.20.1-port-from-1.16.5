package hellfirepvp.astralsorcery.common.world.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

public class RiverbedPlacement extends PlacementModifier {

    public static final Codec<RiverbedPlacement> CODEC = Codec.unit(RiverbedPlacement::new);

    //See ChancePlacement.TYPE: the registered instance has to be the one type() returns.
    public static final PlacementModifierType<RiverbedPlacement> TYPE = () -> CODEC;

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        int x = random.nextInt(16) + pos.getX();
        int z = random.nextInt(16) + pos.getZ();
        int y = context.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        if (y <= 0) {
            return Stream.of();
        }

        BlockPos floor = new BlockPos(x, y - 4, z);

        boolean foundWater = false;
        for (int yy = 0; yy < 5; yy++) {
            BlockPos check = floor.relative(Direction.UP, yy);
            BlockState state = context.getBlockState(check);
            Block block = state.getBlock();
            if (state.getFluidState().is(FluidTags.WATER) || state.is(BlockTags.ICE)) {
                foundWater = true;
                floor = check.below();
                break;
            }
        }
        if (foundWater && context.getBlockState(floor).is(BlockTags.SAND)) {
            return Stream.of(floor);
        }
        return Stream.of();
    }

    @Override
    public PlacementModifierType<?> type() {
        return TYPE;
    }
}
