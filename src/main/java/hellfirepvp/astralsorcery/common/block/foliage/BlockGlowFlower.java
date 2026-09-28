package hellfirepvp.astralsorcery.common.block.foliage;

import hellfirepvp.astralsorcery.common.block.base.template.BlockFlowerTemplate;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMisc;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nonnull;

/**
 * Port of 1.16.5 {@code BlockGlowFlower}.
 */
public class BlockGlowFlower extends BlockFlowerTemplate implements IPlantable {

    private final VoxelShape shape;

    public BlockGlowFlower() {
        super(PropertiesMisc.defaultTickingPlant()
                .lightLevel(state -> 5)
                .offsetType(OffsetType.XZ));
        this.shape = createShape();
    }

    private VoxelShape createShape() {
        return Block.box(1.5, 0, 1.5, 14.5, 13, 14.5);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        Vec3 offset = state.getOffset(world, pos);
        return this.shape.move(offset.x, offset.y, offset.z);
    }

    @Nonnull
    @Override
    public MobEffect getSuspiciousEffect() {
        return MobEffects.LUCK;
    }

    @Override
    public int getEffectDuration() {
        return 40;
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader world, RandomSource random, BlockPos pos, int fortune, int silktouch) {
        if (silktouch == 0) {
            return 0;
        }
        if (fortune > 0) {
            return fortune * Mth.nextInt(random, 2, 5);
        }
        return Mth.nextInt(random, 1, 2);
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return PlantType.CAVE;
    }

}
