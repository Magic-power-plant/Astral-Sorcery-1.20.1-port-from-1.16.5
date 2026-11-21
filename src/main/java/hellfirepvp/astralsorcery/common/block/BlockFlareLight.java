package hellfirepvp.astralsorcery.common.block;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.properties.PropertiesMisc;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.util.color.ColorUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

import java.awt.Color;
import java.util.function.Consumer;

/**
 * Port of 1.16.5 {@code BlockFlareLight}: the invisible light source placed by the illumination wand.
 * <p>
 * 1.16.5 marked the block as replaceable by logs/leaves (tree growth); 1.20.1 handles that through the
 * {@code minecraft:replaceable_by_trees} block tag, see the data tag file for this block.
 */
public class BlockFlareLight extends Block {

    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
    private static final VoxelShape SHAPE = Shapes.create(6F / 16F, 3F / 16F, 6F / 16F, 10F / 16F, 7F / 16F, 10F / 16F);

    public BlockFlareLight() {
        super(PropertiesMisc.defaultAir()
                .lightLevel(state -> 15)
                .noLootTable()
                .noOcclusion());
        registerDefaultState(this.getStateDefinition().any().setValue(COLOR, DyeColor.YELLOW));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        Color c = ColorUtils.flareColorFromDye(state.getValue(COLOR));
        for (int i = 0; i < 2; i++) {
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(pos)
                            .add(0.5, 0.2, 0.5)
                            .add(rand.nextFloat() * 0.1 * (rand.nextBoolean() ? 1 : -1),
                                    rand.nextFloat() * 0.1 * (rand.nextBoolean() ? 1 : -1),
                                    rand.nextFloat() * 0.1 * (rand.nextBoolean() ? 1 : -1)))
                    .setScaleMultiplier(0.4F + rand.nextFloat() * 0.1F)
                    .setAlphaMultiplier(0.35F)
                    .setMotion(new Vector3(0, rand.nextFloat() * 0.01F, 0))
                    .color(VFXColorFunction.constant(c))
                    .setMaxAge(50 + rand.nextInt(20));
        }
        if (rand.nextBoolean()) {
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(pos)
                            .add(0.5, 0.3, 0.5)
                            .add(rand.nextFloat() * 0.02 * (rand.nextBoolean() ? 1 : -1),
                                    rand.nextFloat() * 0.1 * (rand.nextBoolean() ? 1 : -1),
                                    rand.nextFloat() * 0.02 * (rand.nextBoolean() ? 1 : -1)))
                    .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                    .setMotion(new Vector3(0, rand.nextFloat() * 0.01F, 0))
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(25 + rand.nextInt(10));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
                return true;
            }

            @Override
            public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
                return true;
            }
        });
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
        return true;
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> ct) {
        ct.add(COLOR);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
