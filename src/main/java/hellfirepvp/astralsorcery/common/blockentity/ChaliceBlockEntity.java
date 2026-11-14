/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXScaleFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.ColorizationHelper;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.auxiliary.ChaliceHelper;
import hellfirepvp.astralsorcery.common.block.BlockFountain;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.fluid.FluidLiquidStarlight;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.blockentity.base.TickableBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicates;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.blockentity.FluidTankAccess;
import hellfirepvp.astralsorcery.common.util.blockentity.SimpleSingleFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ChaliceBlockEntity
 * Created by HellFirePvP
 * Date: 09.11.2019 / 15:03
 */
public class ChaliceBlockEntity extends TickableBlockEntity {

    //1.16.5: FluidAttributes.BUCKET_VOLUME -> 1.20.1: FluidType.BUCKET_VOLUME (same value, 1000)
    private static final int TANK_SIZE = 64 * FluidType.BUCKET_VOLUME;

    private final SimpleSingleFluidTank tank;
    private final FluidTankAccess access;

    private int nextInteraction = -1;

    private Vector3 rotation = new Vector3();
    private Vector3 prevRotation = new Vector3();
    private Vector3 rotationVec = null;

    public ChaliceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.CHALICE.get(), pos, state);

        this.tank = new SimpleSingleFluidTank(TANK_SIZE);
        this.tank.addUpdateFunction(this::markForUpdate);
        this.access = new FluidTankAccess();
        this.access.putTank(0, tank, Direction.DOWN);
    }

    @Override
    public void tick() {
        super.tick();

        //1.16.5: getWorld().isRemote() -> 1.20.1: getLevel().isClientSide()
        if (getLevel().isClientSide()) {
            if (this.rotationVec == null) {
                this.rotationVec = Vector3.random().normalize().multiply(1.5F);
            }

            this.prevRotation = this.rotation.clone();
            this.rotation.add(this.rotationVec);
        } else {
            if (nextInteraction == -1) {
                nextInteraction = ticksExisted + 20 + rand.nextInt(40);
            }
            if (ticksExisted < nextInteraction) {
                return;
            }
            nextInteraction = ticksExisted + 20 + rand.nextInt(40);

            if (!tickLightwellDraw() && !tickFountainDraw()) {
                tickChaliceInteractions();
            }
        }
    }

    private void tickChaliceInteractions() {
        //1.16.5: World#isBlockPowered -> 1.20.1: Level#hasNeighborSignal; TileEntity#pos/getPos() -> worldPosition/
        //getBlockPos(); BlockPos#down() -> BlockPos#below()
        if (getLevel().hasNeighborSignal(worldPosition) || getLevel().getBlockState(getBlockPos().below()).getBlock() instanceof BlockFountain) {
            return;
        }
        FluidStack thisFluid = this.getTank().getFluid();
        if (thisFluid.isEmpty()) {
            return;
        }

        List<BlockPos> chalicePositions = ChaliceHelper.findNearbyChalices(getLevel(), getBlockPos(), 16);
        Collections.shuffle(chalicePositions, rand);
        for (BlockPos otherChalicePos : chalicePositions) {
            ChaliceBlockEntity otherChalice = MiscUtils.getBlockEntityAt(getLevel(), otherChalicePos, ChaliceBlockEntity.class, false);
            if (otherChalice == null) {
                continue;
            }
            FluidStack otherFluid = otherChalice.getTank().getFluid();
            if (otherFluid.isEmpty()) {
                continue;
            }
            //1.16.5:
            //    LiquidInteractionContext ctx = new LiquidInteractionContext(thisFluid, otherFluid);
            //    List<LiquidInteraction> recipes = RecipeTypesAS.TYPE_LIQUID_INTERACTION.findMatchingRecipes(ctx);
            //This tree has neither LiquidInteractionContext nor AS' CustomRecipeType wrapper (common/crafting is
            //another agent's lane), so the scan findMatchingRecipes did - every recipe of the type whose
            //matches(...) accepts the pair, in registry order - is inlined against the vanilla RecipeManager. The
            //copy into an ArrayList is required because getAllRecipesFor returns an immutable list and the loop
            //below removes from it.
            List<LiquidInteraction> recipes = new ArrayList<>();
            for (LiquidInteraction candidate : getLevel().getRecipeManager().getAllRecipesFor(RecipeTypesAS.LIQUID_INTERACTION.get())) {
                if (candidate.matches(thisFluid, otherFluid)) {
                    recipes.add(candidate);
                }
            }
            LiquidInteraction recipe = LiquidInteraction.pickRecipe(recipes);
            while (recipe != null) {
                if (recipe.consumeInputs(this, otherChalice)) {

                    Vector3 thisChaliceV = new Vector3(this).add(0.5, 1.5, 0.5);
                    Vector3 otherChaliceV = new Vector3(otherChalicePos).add(0.5, 1.5, 0.5);
                    Vector3 target = thisChaliceV.getMidpoint(otherChaliceV);

                    //1.16.5: recipe.getResult().doResult(getWorld(), target.clone()) - InteractionResult#doResult
                    //takes a Vec3 in this tree instead of a Vector3, hence the conversion. toVector3d() builds a
                    //fresh Vec3, so the callee still cannot mutate `target` (which is what the .clone() protected).
                    recipe.getResult().doResult(getLevel(), target.toVector3d());

                    PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.LIQUID_INTERACTION_LINE).addData(buf -> {
                        ByteBufUtils.writeVector(buf, thisChaliceV);
                        ByteBufUtils.writeVector(buf, target);
                        ByteBufUtils.writeFluidStack(buf, thisFluid);
                        ByteBufUtils.writeVector(buf, otherChaliceV);
                        ByteBufUtils.writeVector(buf, target);
                        ByteBufUtils.writeFluidStack(buf, otherFluid);
                    });
                    //1.16.5: PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(world, pos, 32)).
                    //This tree's PacketChannel#sendToAllAround(msg, level, Vec3, range) is the same distributor call;
                    //its target point takes the precise vector rather than 1.16.5's block position.
                    PacketChannel.sendToAllAround(pkt, getLevel(), target.toVector3d(), 32);
                    return;
                }
                recipes.remove(recipe);
                recipe = LiquidInteraction.pickRecipe(recipes);
            }
        }
    }

    private boolean tickFountainDraw() {
        if (getLevel().hasNeighborSignal(worldPosition)) {
            return false;
        }

        Vector3 thisVector = new Vector3(this).add(0.5, 1.5, 0.5);
        List<BlockPos> fountains = BlockDiscoverer.searchForBlocksAround(getLevel(), worldPosition, 16,
                BlockPredicates.isBlock(BlocksAS.FOUNTAIN.get()));
        fountains.removeIf(pos -> {
            Vector3 fountainVec = new Vector3(pos).add(0.5, 0.5, 0.5);
            RaytraceAssist assist = new RaytraceAssist(thisVector, fountainVec);
            return !assist.isClear(getLevel());
        });
        Collections.shuffle(fountains, rand);

        for (BlockPos wellPos : fountains) {
            FountainBlockEntity fountain = MiscUtils.getBlockEntityAt(getLevel(), wellPos, FountainBlockEntity.class, true);
            if (fountain != null) {
                FluidStack drained = fountain.getTank().drain(400, IFluidHandler.FluidAction.SIMULATE);
                if (drained.getAmount() > 100) {
                    int maxFillable = this.getTank().fill(drained, IFluidHandler.FluidAction.SIMULATE);
                    if (maxFillable > 0) {
                        FluidStack actual = fountain.getTank().drain(new FluidStack(drained, maxFillable), IFluidHandler.FluidAction.EXECUTE);
                        this.getTank().fill(actual, IFluidHandler.FluidAction.EXECUTE);

                        Vector3 wellVec = new Vector3(wellPos).add(0.5, 0.5, 0.5);

                        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.LIQUID_INTERACTION_LINE).addData(buf -> {
                            ByteBufUtils.writeVector(buf, wellVec);
                            ByteBufUtils.writeVector(buf, thisVector);
                            ByteBufUtils.writeFluidStack(buf, actual);
                        });
                        PacketChannel.sendToAllAround(pkt, getLevel(), wellVec.toVector3d(), 32);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean tickLightwellDraw() {
        if (getLevel().hasNeighborSignal(worldPosition)) {
            return false;
        }
        FluidStack thisFluid = this.getTank().getFluid();
        //1.16.5 tested `thisFluid.getFluid() instanceof FluidLiquidStarlight` - back then the mod's fluid class was
        //the Fluid itself. In 1.20.1 the source/flowing fluids are plain ForgeFlowingFluid instances and
        //FluidLiquidStarlight is their shared FluidType, so the same identity test is on the fluid type.
        if (!thisFluid.isEmpty() && (!(thisFluid.getFluid().getFluidType() instanceof FluidLiquidStarlight) || thisFluid.getAmount() + 100 >= TANK_SIZE)) {
            return false;
        }

        Vector3 thisVector = new Vector3(this).add(0.5, 1.5, 0.5);
        List<BlockPos> wellPositions = BlockDiscoverer.searchForBlocksAround(getLevel(), worldPosition, 16,
                BlockPredicates.isBlock(BlocksAS.WELL.get()));
        wellPositions.removeIf(pos -> {
            Vector3 wellVec = new Vector3(pos).add(0.5, 0.5, 0.5);
            RaytraceAssist assist = new RaytraceAssist(thisVector, wellVec);
            return !assist.isClear(getLevel());
        });
        Collections.shuffle(wellPositions, rand);

        for (BlockPos wellPos : wellPositions) {
            WellBlockEntity well = MiscUtils.getBlockEntityAt(getLevel(), wellPos, WellBlockEntity.class, true);
            if (well != null) {
                FluidStack drained = well.getTank().drain(400, IFluidHandler.FluidAction.SIMULATE);
                if (drained.getFluid().getFluidType() instanceof FluidLiquidStarlight && drained.getAmount() > 100) {
                    int maxFillable = this.getTank().getMaxAddable(drained.getAmount());
                    if (maxFillable > 0) {
                        FluidStack actual = well.getTank().drain(new FluidStack(drained, maxFillable), IFluidHandler.FluidAction.EXECUTE);
                        this.getTank().fill(actual, IFluidHandler.FluidAction.EXECUTE);

                        Vector3 wellVec = new Vector3(wellPos).add(0.5, 0.5, 0.5);

                        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.LIQUID_INTERACTION_LINE).addData(buf -> {
                            ByteBufUtils.writeVector(buf, wellVec);
                            ByteBufUtils.writeVector(buf, thisVector);
                            ByteBufUtils.writeFluidStack(buf, actual);
                        });
                        PacketChannel.sendToAllAround(pkt, getLevel(), wellVec.toVector3d(), 32);
                        return true;
                    }
                    return false; //Cannot fill from any other either in this case.
                }
            }
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static void drawLiquidLine(PktPlayEffect pktPlayEffect) {
        //1.16.5: PacketBuffer -> 1.20.1: FriendlyByteBuf
        FriendlyByteBuf buf = pktPlayEffect.getExtraData();
        while (buf.isReadable()) {
            Vector3 from = ByteBufUtils.readVector(pktPlayEffect.getExtraData());
            Vector3 to = ByteBufUtils.readVector(pktPlayEffect.getExtraData());
            FluidStack fluid = ByteBufUtils.readFluidStack(pktPlayEffect.getExtraData());
            VFXColorFunction<?> colorFn = VFXColorFunction.constant(ColorizationHelper.getColor(fluid).orElse(Color.WHITE).brighter());

            playLineGenericParticles(from, to, 0.1F + rand.nextFloat() * 0.2F, colorFn);
            playLineGenericParticles(from, to, 0.1F + rand.nextFloat() * 0.2F, colorFn);
            playLineFluidParticles(from, to, 0.25F + rand.nextFloat() * 0.2F, fluid);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void playLineGenericParticles(Vector3 from, Vector3 to, float width, VFXColorFunction<?> colorFn) {
        playLineParticles(from, to, width, at -> Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(at)
                .color(colorFn)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .setMotion(Vector3.random().multiply(0.01F))
                .setScaleMultiplier(0.15F + rand.nextFloat() * 0.25F)
                .setMaxAge(20 + rand.nextInt(35)));
    }

    @OnlyIn(Dist.CLIENT)
    private static void playLineFluidParticles(Vector3 from, Vector3 to, float width, FluidStack fluid) {
        //1.16.5: new Color(fluid.getFluid().getAttributes().getColor(fluid)). FluidAttributes is gone in 1.20.1;
        //the client-side tint moved to IClientFluidTypeExtensions#getTintColor(FluidStack) - same ARGB value, and
        //java.awt.Color(int) drops the alpha byte here exactly as it did in 1.16.5.
        Color c = new Color(IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid));
        playLineParticles(from, to, width, at -> Effects.of(EffectTemplatesAS.CUBE_TRANSLUCENT_ATLAS)
                .spawn(at)
                .setTextureAtlasSprite(RenderingUtils.getParticleTexture(fluid))
                .tumble()
                .color(VFXColorFunction.constant(c))
                .setMotion(Vector3.random().multiply(0.01F))
                .setScaleMultiplier(0.2F + rand.nextFloat() * 0.05F)
                .scale(VFXScaleFunction.SHRINK)
                .setMaxAge(10 + rand.nextInt(15)));
    }

    @OnlyIn(Dist.CLIENT)
    private static void playLineParticles(Vector3 from, Vector3 to, float width, Function<Vector3, EntityVisualFX> pCreator) {
        to.clone().subtract(from).stepAlongVector(width, v -> pCreator.apply(from.clone().add(v)));
    }

    @Nonnull
    public SimpleSingleFluidTank getTank() {
        return this.tank;
    }

    @Nonnull
    public IFluidHandler getTankAccess() {
        return this.access.getCapability(Direction.DOWN).orElse(null);
    }

    @Nonnull
    public Vector3 getRotation() {
        return rotation;
    }

    @Nonnull
    public Vector3 getPrevRotation() {
        return prevRotation;
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.tank.readNBT(compound.getCompound("tank"));
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        compound.put("tank", this.tank.writeNBT());
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
