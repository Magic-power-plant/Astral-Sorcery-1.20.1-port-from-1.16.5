/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.fountain;

import hellfirepvp.astralsorcery.common.block.fountain.BlockFountainPrime;
import hellfirepvp.astralsorcery.common.crafting.nojson.FountainEffectRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.fountain.FountainEffect;
import hellfirepvp.astralsorcery.common.fluid.FluidLiquidStarlight;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.blockentity.base.TickableBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.blockentity.capability.FluidTankAccess;
import hellfirepvp.astralsorcery.common.blockentity.fluid.SimpleSingleFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FountainBlockEntity
 * Created by HellFirePvP
 * Date: 29.10.2020 / 19:56
 */
public class FountainBlockEntity extends TickableBlockEntity {

    //1.16.5: FluidAttributes.BUCKET_VOLUME -> 1.20.1: FluidType.BUCKET_VOLUME (Forge moved the fluid attribute
    //constants onto FluidType when FluidAttributes was removed in 1.19.2)
    private static final int TANK_SIZE = 16 * FluidType.BUCKET_VOLUME;
    private static final int LIQUID_STARLIGHT_TANK_SIZE = 16 * FluidType.BUCKET_VOLUME;

    private FountainEffect<?> currentEffect;
    private FountainEffect.EffectContext effectContext;

    private int tickDrawLiquidStarlight = 0;
    private int tickActiveFountainEffect = 0;

    private int mbLiquidStarlight = 0;
    private final FluidTankAccess access;
    private final SimpleSingleFluidTank tank;

    //1.16.5: TileEntity() took only the TileEntityType. 1.20.1 BlockEntity requires (type, pos, state).
    public FountainBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.FOUNTAIN.get(), pos, state);

        this.tank = new SimpleSingleFluidTank(TANK_SIZE);
        this.tank.addUpdateFunction(this::markForUpdate);
        this.access = new FluidTankAccess();
        this.access.putTank(0, tank);
    }

    @Override
    public void tick() {
        super.tick();

        //1.16.5: getWorld().isRemote() -> 1.20.1: BlockEntity#level + Level#isClientSide()
        if (!this.level.isClientSide()) {
            if (this.hasMultiblock()) {
                this.updateFountainComponents();
                this.drawLiquidStarlight();

                FountainEffect effect = this.getCurrentEffect();
                if (effect != null) {
                    FountainEffect.EffectContext ctx = this.effectContext;
                    if (!this.consumeLiquidStarlight(1)) {
                        this.setCurrentEffect(null);
                        this.replaceCurrentEffect(effect, ctx, null);
                        return;
                    }

                    FountainEffect.OperationSegment segment = getSegment();
                    if (segment != FountainEffect.OperationSegment.RUNNING) {
                        this.tickActiveFountainEffect++;
                    }
                    FountainEffect.OperationSegment nextSegment = getSegment();

                    if (segment != nextSegment) {
                        effect.transition(this, ctx, LogicalSide.SERVER, segment, nextSegment);
                        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.FOUNTAIN_TRANSITION_SEGMENT).addData(buf -> {
                            ByteBufUtils.writePos(buf, this.worldPosition);
                            ByteBufUtils.writeEnumValue(buf, segment);
                            ByteBufUtils.writeEnumValue(buf, nextSegment);
                        });
                        //1.16.5: PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(world, pos, 32)).
                        //1.16.5 pointFromPos(World, Vector3i, double) used the raw block coordinates, hence
                        //Vec3#atLowerCornerOf (not atCenterOf) for an identical TargetPoint.
                        PacketChannel.sendToAllAround(pkt, this.level, Vec3.atLowerCornerOf(this.worldPosition), 32);
                    }
                    effect.tick(this, ctx, this.tickActiveFountainEffect, LogicalSide.SERVER, this.getSegment());
                }
            }
        } else {
            if (this.hasMultiblock()) {
                FountainEffect effect = this.getCurrentEffect();
                if (effect != null) {
                    effect.tick(this, this.effectContext, this.tickActiveFountainEffect, LogicalSide.CLIENT, getSegment());
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void playTransitionEffect(PktPlayEffect pktPlayEffect) {
        BlockPos at = ByteBufUtils.readPos(pktPlayEffect.getExtraData());
        FountainEffect.OperationSegment segment = ByteBufUtils.readEnumValue(pktPlayEffect.getExtraData(), FountainEffect.OperationSegment.class);
        FountainEffect.OperationSegment nextSegment = ByteBufUtils.readEnumValue(pktPlayEffect.getExtraData(), FountainEffect.OperationSegment.class);

        //1.16.5: Minecraft#world -> 1.20.1: Minecraft#level
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        FountainBlockEntity fountain = MiscUtils.getBlockEntityAt(world, at, FountainBlockEntity.class, false);
        if (fountain == null) {
            return;
        }
        FountainEffect effect = fountain.getCurrentEffect();
        if (effect == null) {
            return;
        }
        effect.transition(fountain, fountain.effectContext, LogicalSide.CLIENT, segment, nextSegment);
    }

    @OnlyIn(Dist.CLIENT)
    public static void replaceEffect(PktPlayEffect pktPlayEffect) {
        BlockPos at = ByteBufUtils.readPos(pktPlayEffect.getExtraData());
        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        FountainBlockEntity fountain = MiscUtils.getBlockEntityAt(world, at, FountainBlockEntity.class, false);
        if (fountain == null) {
            return;
        }
        FountainEffect effect = fountain.getCurrentEffect();
        if (effect == null) {
            return;
        }
        effect.onReplace(fountain, fountain.effectContext, null, LogicalSide.CLIENT);
    }

    private void drawLiquidStarlight() {
        this.tickDrawLiquidStarlight--;

        if (this.tickDrawLiquidStarlight <= 0) {
            this.tickDrawLiquidStarlight = 100;

            if (this.mbLiquidStarlight < (LIQUID_STARLIGHT_TANK_SIZE * 0.8F) && this.currentEffect != null) {
                //1.16.5: BlockPos#up() -> 1.20.1: BlockPos#above()
                ChaliceBlockEntity chalice = MiscUtils.getBlockEntityAt(level, worldPosition.above(), ChaliceBlockEntity.class, false);
                if (chalice != null) {
                    FluidStack fluid = chalice.getTank().drain(400, IFluidHandler.FluidAction.SIMULATE);
                    //1.16.5 tested `fluid.getFluid() instanceof FluidLiquidStarlight` - back then the mod's fluid
                    //class WAS the Fluid. In 1.20.1 the source/flowing pair are plain ForgeFlowingFluid instances and
                    //FluidLiquidStarlight is their shared FluidType, so the same identity test is on the fluid type.
                    if (!fluid.isEmpty() && fluid.getFluid().getFluidType() instanceof FluidLiquidStarlight) {
                        FluidStack drained = chalice.getTank().drain(new FluidStack(fluid, 400), IFluidHandler.FluidAction.EXECUTE);
                        this.mbLiquidStarlight += drained.getAmount();
                        this.markForUpdate();
                    }
                }
            }
        }
    }

    private void updateFountainComponents() {
        FountainEffect prevEffect = this.getCurrentEffect();
        FountainEffect.EffectContext prevContext = this.effectContext;

        //1.16.5: BlockPos#down() -> 1.20.1: BlockPos#below()
        BlockState primeState = level.getBlockState(worldPosition.below());
        if (primeState.getBlock() instanceof BlockFountainPrime) {
            if (this.setCurrentEffect(((BlockFountainPrime) primeState.getBlock()).provideEffect()) && prevEffect != null) {
                this.replaceCurrentEffect(prevEffect, prevContext, this.getCurrentEffect());
            }
        } else {
            if (this.setCurrentEffect(null) && prevEffect != null) {
                this.replaceCurrentEffect(prevEffect, prevContext, null);
            }
        }
    }

    private void replaceCurrentEffect(FountainEffect prevEffect, FountainEffect.EffectContext prevContext, FountainEffect newEffect) {
        prevEffect.onReplace(this, prevContext, newEffect, LogicalSide.SERVER);
        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.FOUNTAIN_REPLACE_EFFECT)
                .addData(buf -> ByteBufUtils.writePos(buf, this.worldPosition));
        PacketChannel.sendToAllAround(pkt, this.level, Vec3.atLowerCornerOf(this.worldPosition), 32);

    }

    private boolean setCurrentEffect(@Nullable FountainEffect<?> effect) {
        if (!Objects.equals(this.currentEffect, effect)) {
            this.tickActiveFountainEffect = 0;
            this.currentEffect = effect;
            if (this.currentEffect == null) {
                this.effectContext = null;
            } else {
                this.effectContext = this.currentEffect.createContext(this);
            }
            this.markForUpdate();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        return StructureTypesAS.PTYPE_FOUNTAIN;
    }

    public boolean consumeLiquidStarlight(int amount) {
        if (amount <= 0) {
            return this.mbLiquidStarlight > 0;
        }
        if (this.mbLiquidStarlight >= amount) {
            this.mbLiquidStarlight -= amount;
            this.markForUpdate();
            return true;
        }
        this.mbLiquidStarlight = 0;
        this.markForUpdate();
        return false;
    }

    public FountainEffect.OperationSegment getSegment() {
        if (this.getCurrentEffect() == null || this.tickActiveFountainEffect == 0) {
            return FountainEffect.OperationSegment.INACTIVE;
        }
        if (this.getCurrentEffect().isInTick(FountainEffect.OperationSegment.STARTUP, this.tickActiveFountainEffect)) {
            return FountainEffect.OperationSegment.STARTUP;
        }
        if (this.getCurrentEffect().isInTick(FountainEffect.OperationSegment.PREPARATION, this.tickActiveFountainEffect)) {
            return FountainEffect.OperationSegment.PREPARATION;
        }
        return FountainEffect.OperationSegment.RUNNING;
    }

    @Nullable
    public FountainEffect<?> getCurrentEffect() {
        return currentEffect;
    }

    public int getTickActiveFountainEffect() {
        return tickActiveFountainEffect;
    }

    @Nonnull
    public SimpleSingleFluidTank getTank() {
        return tank;
    }

    //1.16.5 declares read/writeCustomNBT public; TileEntitySynchronized in this tree declares them protected,
    //so the overrides keep the inherited (narrower) visibility.
    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.tickActiveFountainEffect = compound.getInt("tickActiveFountainEffect");
        this.mbLiquidStarlight = compound.getInt("mbLiquidStarlight");
        this.tank.readNBT(compound.getCompound("tank"));
        if (compound.contains("currentEffect")) {
            FountainEffect<?> prevEffect = this.getCurrentEffect();
            ResourceLocation key = new ResourceLocation(compound.getString("currentEffect"));
            this.currentEffect = FountainEffectRegistry.getEffect(key);

            if (this.currentEffect != null) {
                if (!Objects.equals(this.currentEffect, prevEffect)) {
                    this.effectContext = this.currentEffect.createContext(this);
                }
                this.effectContext.readFromNBT(compound.getCompound("currentEffectData"));
            } else {
                this.effectContext = null;
            }
        } else {
            this.currentEffect = null;
            this.effectContext = null;
        }
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        compound.putInt("tickActiveFountainEffect", this.tickActiveFountainEffect);
        compound.putInt("mbLiquidStarlight", this.mbLiquidStarlight);
        compound.put("tank", this.tank.writeNBT());
        if (this.currentEffect != null) {
            compound.putString("currentEffect", this.currentEffect.getId().toString());
            if (this.effectContext != null) {
                CompoundTag tag = new CompoundTag();
                this.effectContext.writeToNBT(tag);
                compound.put("currentEffectData", tag);
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (this.access.hasCapability(cap, side)) {
            return getCachedCapability(new CapabilityKey(cap, side), () -> this.access.getCapability(side).orElseThrow(() -> new IllegalStateException("Capability availability changed during lookup"))).cast();
        }
        return super.getCapability(cap, side);
    }
}
