package hellfirepvp.astralsorcery.common.block.base;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.blockentity.base.FakedStateBlockEntity;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public abstract class BlockFakedState extends BaseEntityBlock {

    protected BlockFakedState(Properties builder) {
        super(builder);
    }

    @OnlyIn(Dist.CLIENT)
    protected void playParticles(Level world, BlockPos pos, RandomSource rand) {
        if (rand.nextInt(8) == 0) {
            VFXColorFunction<?> colorFn = VFXColorFunction.WHITE;
            FakedStateBlockEntity fakedState = MiscUtils.getBlockEntityAt(world, pos, FakedStateBlockEntity.class, false);
            if (fakedState != null) {
                colorFn = VFXColorFunction.constant(fakedState.getOverlayColor());
            }
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(Vector3.random().abs().add(pos))
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .color(colorFn)
                    .setScaleMultiplier(0.2F + rand.nextFloat() * 0.05F)
                    .setMaxAge(25 + rand.nextInt(5));
        }
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level world, BlockPos pos, ParticleEngine manager) {
                BlockState fakeState = BlockFakedState.this.getFakedState(world, pos);
                RenderingUtils.playBlockBreakParticles(pos, state, fakeState);
                return true;
            }

            @Override
            public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
                return true;
            }
        });
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level world, BlockPos pos, Entity entity) {
        return true;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        BlockState fakeState = this.getFakedState(world, pos);
        return fakeState.getSoundType(world, pos, entity);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockState fakeState = this.getFakedState(world, pos);
        return fakeState.getShape(world, pos, context);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return Lists.newArrayList();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        BlockState fakeState = this.getFakedState(worldIn, pos);
        try {
            return fakeState.getCollisionShape(worldIn, pos, context);
        } catch (Exception exc) {
            //Ignore the result if this happens to be more complex than expected
        }
        return super.getCollisionShape(state, worldIn, pos, context);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        BlockState fakeState = this.getFakedState(worldIn, pos);
        try {
            return fakeState.getVisualShape(worldIn, pos, context);
        } catch (Exception exc) {
            //Ignore the result if this happens to be more complex than expected
        }
        return super.getVisualShape(state, worldIn, pos, context);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        BlockState fakeState = this.getFakedState(world, pos);
        try {
            return fakeState.getBlock().use(fakeState, world, pos, player, handIn, hit);
        } catch (Exception exc) {
            //Ignore the result if we can't interact
        }
        return super.use(state, world, pos, player, handIn, hit);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        BlockState fakeState = this.getFakedState(world, pos);
        try {
            return fakeState.getBlock().getCloneItemStack(fakeState, target, world, pos, player);
        } catch (Exception exc) {
            //Ignore the result. If we can't pick that stuff here, well.. guess we can't at all.
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    protected BlockState getFakedState(BlockGetter world, BlockPos pos) {
        FakedStateBlockEntity tb = MiscUtils.getBlockEntityAt(world, pos, FakedStateBlockEntity.class, true);
        return tb != null ? tb.getFakedState() : Blocks.AIR.defaultBlockState();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

}
