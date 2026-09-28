package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.tile.BlockLens;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeTile;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.item.lens.LensColorType;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.tile.base.network.TileTransmissionBase;
import hellfirepvp.astralsorcery.common.tile.network.StarlightTransmissionLens;
import hellfirepvp.astralsorcery.common.util.PartialEffectExecutor;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class TileLens extends TileTransmissionBase<IPrismTransmissionNode> implements CrystalAttributeTile {

    private CrystalAttributes attributes = null;
    private LensColorType colorType = null;

    private float accumulatedStarlight = 0;

    private List<BlockPos> occupiedConnections = new LinkedList<>();

    public TileLens(BlockPos pos, BlockState state) {
        this(TileEntityTypesAS.LENS.get(), pos, state);
    }

    protected TileLens(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (colorType != null) {
            if (level.isClientSide()) {
                playColorEffects();
            }
            doColorEffects();
        }
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();

        this.markForUpdate();
    }

    public void transmissionTick(float starlightAmt, IWeakConstellation type) {
        this.accumulatedStarlight += starlightAmt;
        this.markForUpdate();
    }

    private void doColorEffects() {
        Level world = this.getLevel();
        if (!world.isClientSide() && !this.occupiedConnections.isEmpty()) {
            this.occupiedConnections.clear();
            markForUpdate();
        }

        if (accumulatedStarlight <= 0) {
            return;
        }
        float effectMultiplier = accumulatedStarlight * 1.4F;
        accumulatedStarlight = 0;

        List<BlockPos> linked = getLinkedPositions();
        if (linked.isEmpty()) {
            return;
        }

        Vector3 thisVec = new Vector3(this).add(0.5, 0.5, 0.5);

        for (BlockPos linkedTo : linked) {
            PartialEffectExecutor exec = new PartialEffectExecutor((1F / ((float) linked.size())) * effectMultiplier, rand);

            Vector3 to = new Vector3(linkedTo).add(0.5, 0.5, 0.5);
            RaytraceAssist rta = new RaytraceAssist(thisVec, to).includeEndPoint();
            if (colorType.getType().doBlockInteraction()) {
                if (!rta.isClear(world) && rta.positionHit() != null) {
                    BlockPos posHit = rta.positionHit();

                    BlockState stateHit = world.getBlockState(posHit);
                    colorType.blockInBeam(world, posHit, stateHit, exec);

                    if (!world.isClientSide()) {
                        this.occupiedConnections.add(posHit);
                    }
                } else {
                    if (!world.isClientSide()) {
                        this.occupiedConnections.add(linkedTo);
                    }
                }
            }
            if (colorType.getType().doEntityInteraction()) {
                exec.reset();

                rta.setCollectEntities(0.5);
                rta.isClear(world);
                List<Entity> found = rta.collectedEntities(world);
                found.forEach(e -> colorType.entityInBeam(world, thisVec, to, e, exec));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playColorEffects() {
        Vector3 at = new Vector3(this).add(0.5, 0.5, 0.5);
        Color lensColor = this.colorType.getColor();

        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(this)
                        .add(0.2, 0.2, 0.2)
                        .add(rand.nextFloat() * 0.6, rand.nextFloat() * 0.6, rand.nextFloat() * 0.6))
                .color(VFXColorFunction.constant(lensColor))
                .setScaleMultiplier(0.1F + rand.nextFloat() * 0.15F);

        if (getTicksExisted() % 40 == 0) {
            for (BlockPos connected : this.occupiedConnections) {
                Vector3 to = new Vector3(connected).add(0.5, 0.5, 0.5);
                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(at)
                        .setup(to, 0.6, 0.6)
                        .color(VFXColorFunction.constant(lensColor));
            }
        }
    }

    public LensColorType setColorType(@Nullable LensColorType colorType) {
        if (this.getColorType() == colorType) {
            return colorType;
        }
        LensColorType prev = this.getColorType();
        this.colorType = colorType;
        this.markForUpdate();
        return prev;
    }

    @Nullable
    public LensColorType getColorType() {
        return colorType;
    }

    public Direction getPlacedAgainst() {
        BlockState state = level.getBlockState(getBlockPos());
        if (!(state.getBlock() instanceof BlockLens)) {
            return Direction.DOWN;
        }
        return state.getValue(BlockLens.PLACED_AGAINST);
    }

    @Override
    public boolean isSingleLink() {
        return true;
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(@Nullable CrystalAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.attributes = CrystalAttributes.getCrystalAttributes(compound);
        if (compound.contains("colorType")) {
            this.colorType = LensColorType.byName(ResourceLocation.tryParse(compound.getString("colorType")));
        } else {
            this.colorType = null;
        }
        this.occupiedConnections = NBTHelper.readList(compound, "occupiedConnections", Tag.TAG_COMPOUND,
                nbt -> NBTHelper.readBlockPosFromNBT((CompoundTag) nbt));
    }

    @Override
    protected void readNetNBT(CompoundTag compound) {
        super.readNetNBT(compound);
        this.accumulatedStarlight = compound.getFloat("accumulatedStarlight");
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        if (this.attributes != null) {
            this.attributes.store(compound);
        }
        if (this.colorType != null) {
            compound.putString("colorType", this.colorType.getName().toString());
        }
        NBTHelper.writeList(compound, "occupiedConnections", this.occupiedConnections,
                pos -> NBTHelper.writeBlockPosToNBT(pos, new CompoundTag()));
    }

    @Override
    protected void writeNetNBT(CompoundTag compound) {
        super.writeNetNBT(compound);
        compound.putFloat("accumulatedStarlight", this.accumulatedStarlight);
    }

    @Nonnull
    @Override
    public IPrismTransmissionNode provideTransmissionNode(BlockPos at) {
        return new StarlightTransmissionLens(at, this.attributes);
    }
}
