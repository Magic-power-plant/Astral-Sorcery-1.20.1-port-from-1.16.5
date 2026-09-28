package hellfirepvp.astralsorcery.common.fluid;

import hellfirepvp.astralsorcery.common.crafting.nojson.LiquidStarlightCraftingRegistry;
import hellfirepvp.astralsorcery.common.data.config.entry.CraftingConfig;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.EmptyFluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Supplier;

public class BlockLiquidStarlight extends LiquidBlock {

    public BlockLiquidStarlight(Supplier<? extends FlowingFluid> fluidSupplier) {
        super(fluidSupplier, Properties.of()
                .noCollission()
                .strength(100.0F)
                .lightLevel(state -> 15)
                .noLootTable());
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (state.getValue(LEVEL) != 0) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, true));
        } else if (entity instanceof ItemEntity itemEntity) {
            LiquidStarlightCraftingRegistry.tryCraft(itemEntity, pos);

            if (!level.isClientSide() && itemEntity.getItem().isEmpty()) {
                entity.discard();
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (this.reactWithNeighbors(level, pos, state)) {
            level.scheduleTick(pos, state.getFluidState().getType(), this.getFluid().getTickDelay(level));
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (this.reactWithNeighbors(level, pos, state)) {
            level.scheduleTick(pos, state.getFluidState().getType(), this.getFluid().getTickDelay(level));
        }
    }

    private boolean reactWithNeighbors(Level level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            FluidState otherState = level.getFluidState(pos.relative(dir));
            Fluid otherFluid = otherState.getType();
            if (otherFluid instanceof FlowingFluid flowing) {
                otherFluid = flowing.getSource();
            }
            if (otherFluid instanceof EmptyFluid || otherFluid.equals(this.getFluid())) {
                continue;
            }

            boolean isHot = otherFluid.getFluidType().getTemperature() > 600;
            BlockState generate;
            if (isHot) {
                if (CraftingConfig.CONFIG.liquidStarlightInteractionSand.get()) {
                    generate = Blocks.SAND.defaultBlockState();
                    if (CraftingConfig.CONFIG.liquidStarlightInteractionAquamarine.get() && level.random.nextInt(800) == 0) {
                        generate = BlocksAS.AQUAMARINE_SAND_ORE.get().defaultBlockState();
                    }
                } else {
                    generate = Blocks.COBBLESTONE.defaultBlockState();
                }
            } else {
                if (CraftingConfig.CONFIG.liquidStarlightInteractionIce.get()) {
                    generate = Blocks.PACKED_ICE.defaultBlockState();
                } else {
                    generate = Blocks.COBBLESTONE.defaultBlockState();
                }
            }

            level.setBlock(pos, ForgeEventFactory.fireFluidPlaceBlockEvent(level, pos, pos, generate), 3);
        }
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, net.minecraft.util.RandomSource rand) {
        // TODO_STUB[AS-FLUID-01]: Client-side liquid starlight particle effects
    }
}
