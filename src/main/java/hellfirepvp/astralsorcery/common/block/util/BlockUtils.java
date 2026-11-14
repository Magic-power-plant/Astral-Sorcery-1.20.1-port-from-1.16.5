package hellfirepvp.astralsorcery.common.block.util;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class BlockUtils {

    @Nonnull
    public static List<ItemStack> getDrops(ServerLevel world, BlockPos pos, int harvestFortune, Random rand) {
        return getDrops(world, pos, harvestFortune, rand, ItemStack.EMPTY);
    }

    @Nonnull
    public static List<ItemStack> getDrops(ServerLevel world, BlockPos pos, int harvestFortune, Random rand, ItemStack tool) {
        return getDrops(world, pos, world.getBlockState(pos), harvestFortune, rand, tool);
    }

    @Nonnull
    public static List<ItemStack> getDrops(ServerLevel world, BlockPos pos, BlockState state, int harvestFortune, Random rand, ItemStack tool) {
        LootParams.Builder builder = new LootParams.Builder(world)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, MiscUtils.getBlockEntityAt(world, pos, BlockEntity.class, true))
                .withLuck(harvestFortune);
        return state.getDrops(builder);
    }

    @Nonnull
    public static BlockPos getWorldTopPos(BlockPos at) {
        BlockPos it = at;
        while (it.getY() < 320) {
            it = it.above();
        }
        return it;
    }

    public static BlockPos firstSolidDown(BlockGetter world, BlockPos at) {
        BlockState state = world.getBlockState(at);
        while (at.getY() > 0 && !state.blocksMotion() && state.getFluidState().isEmpty()) {
            at = at.below();
            state = world.getBlockState(at);
        }
        return at;
    }

    public static boolean isReplaceable(Level world, BlockPos pos) {
        return isReplaceable(world, pos, world.getBlockState(pos));
    }

    public static boolean isReplaceable(Level world, BlockPos pos, BlockState state) {
        if (world.isEmptyBlock(pos)) {
            return true;
        }
        net.minecraft.world.item.context.BlockPlaceContext ctx = TestBlockUseContext.getHandContext(world, null, InteractionHand.MAIN_HAND, pos, Direction.UP);
        return state.canBeReplaced(ctx);
    }

    public static float getSimpleBreakSpeed(LivingEntity entity, ItemStack tool, BlockState state) {
        float breakSpeed = tool.getDestroySpeed(state);
        if (breakSpeed > 1.0F) {
            float efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, tool);
            if (efficiencyLevel > 0 && !tool.isEmpty()) {
                breakSpeed += efficiencyLevel * efficiencyLevel + 1;
            }
        }

        if (MobEffectUtil.hasDigSpeed(entity)) {
            breakSpeed *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(entity) + 1F) * 0.2F;
        }

        if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float fatigueMultiplier;
            switch (entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0:
                    fatigueMultiplier = (float) Math.pow(0.3F, 1);
                    break;
                case 1:
                    fatigueMultiplier = (float) Math.pow(0.3F, 2);
                    break;
                case 2:
                    fatigueMultiplier = (float) Math.pow(0.3F, 3);
                    break;
                case 3:
                default:
                    fatigueMultiplier = (float) Math.pow(0.3F, 4);
            }

            breakSpeed *= fatigueMultiplier;
        }

        if (entity.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(entity)) {
            breakSpeed /= 5.0F;
        }

        if (!entity.onGround()) {
            breakSpeed /= 5.0F;
        }
        return breakSpeed;
    }

    public static boolean isFluidBlock(Level world, BlockPos pos) {
        return isFluidBlock(world.getBlockState(pos));
    }

    public static boolean isFluidBlock(BlockState state) {
        return !state.getFluidState().isEmpty();
    }

    @Nullable
    public static BlockState getMatchingState(Collection<BlockState> applicableStates, @Nullable BlockState test) {
        for (BlockState state : applicableStates) {
            if (matchStateExact(state, test)) {
                return state;
            }
        }
        return null;
    }

    public static boolean matchStateExact(@Nullable BlockState state, @Nullable BlockState stateToTest) {
        if (state == null) {
            return stateToTest == null;
        } else if (stateToTest == null) {
            return false;
        }

        if (!state.is(stateToTest.getBlock())) {
            return false;
        }

        for (Property<?> prop : state.getProperties()) {
            Comparable<?> original = state.getValue(prop);
            try {
                Comparable<?> test = stateToTest.getValue(prop);
                if (!original.equals(test)) {
                    return false;
                }
            } catch (Exception exc) {
                return false;
            }
        }
        return true;
    }

    public static boolean breakBlockWithPlayer(BlockPos pos, ServerPlayer playerMP) {
        return playerMP.gameMode.destroyBlock(pos);
    }

    public static boolean breakBlockWithoutPlayer(ServerLevel world, BlockPos pos, BlockState state, ItemStack tool, boolean dropBlocks, boolean doDrops, boolean spawnParticles) {
        if (state.isAir()) {
            return false;
        }
        if (doDrops) {
            world.destroyBlock(pos, dropBlocks);
        } else {
            world.removeBlock(pos, false);
        }
        return true;
    }

    public static boolean canToolBreakBlockWithoutPlayer(Level world, BlockPos pos, BlockState state, ItemStack tool) {
        return !state.isAir() && state.getDestroySpeed(world, pos) >= 0 && tool.isCorrectToolForDrops(state);
    }

}
