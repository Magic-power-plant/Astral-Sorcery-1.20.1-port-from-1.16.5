/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.crystal;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.source.FXSource;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalCollector;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.block.crystal.CollectorCrystalType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationBlockEntity;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeBlockEntity;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionSourceNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.IndependentCrystalSource;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.blockentity.base.network.SourceBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CollectorCrystalBlockEntity
 * Created by HellFirePvP
 * Date: 10.08.2019 / 19:55
 */
public class CollectorCrystalBlockEntity extends SourceBlockEntity<SimpleTransmissionSourceNode> implements CrystalAttributeBlockEntity, ConstellationBlockEntity {

    public static final BlockPos[] OFFSETS_LIQUID_STARLIGHT = new BlockPos[] {
            new BlockPos(-1, -4, -1),
            new BlockPos( 0, -4, -1),
            new BlockPos( 1, -4, -1),
            new BlockPos( 1, -4,  0),
            new BlockPos( 1, -4,  1),
            new BlockPos( 0, -4,  1),
            new BlockPos(-1, -4,  1),
            new BlockPos(-1, -4,  0),
    };

    private UUID playerUUID = null;
    private CrystalAttributes crystalAttributes;
    private CollectorCrystalType collectorType = CollectorCrystalType.ROCK_CRYSTAL;
    private IWeakConstellation constellationType;
    private IMinorConstellation constellationTrait;

    //1.16.5 holds the four orbital FX sources in an Object[] rather than an FXOrbitalCollector[] on purpose: the
    //field lives on a class that also exists on the dedicated server, so naming a client-only type here would
    //make the server try to load it when verifying the field descriptor. Kept verbatim.
    private Object[] effectOrbitals = new Object[4];

    public CollectorCrystalBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.COLLECTOR_CRYSTAL.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        //1.16.5: getWorld().isRemote() -> 1.20.1: getLevel().isClientSide()
        if (!this.getLevel().isClientSide()) {
            this.doesSeeSky();
            this.hasMultiblock();
        } else {
            this.playEffects();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playEffects() {
        Vector3 thisPos = new Vector3(this).add(0.5F, 0.5F, 0.5F);
        Vector3 particlePos = thisPos.clone();
        MiscUtils.applyRandomOffset(particlePos, rand, 0.75F);

        if (this.isEnhanced() &&
                this.doesSeeSky() &&
                this.getCollectorType() == CollectorCrystalType.CELESTIAL_CRYSTAL &&
                this.getAttunedConstellation() != null) {

            Color c = this.getAttunedConstellation().getConstellationColor();

            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(particlePos)
                    .setScaleMultiplier(0.2F + rand.nextFloat() * 0.1F)
                    .setAlphaMultiplier(0.8F)
                    .color(VFXColorFunction.constant(c))
                    .setMaxAge(20 + rand.nextInt(10));

            for (int i = 0; i < this.effectOrbitals.length; i++) {
                FXOrbitalCollector fxSource = (FXOrbitalCollector) this.effectOrbitals[i];
                if (fxSource == null) {
                    FXSource<?, ?> src = new FXOrbitalCollector(new Vector3(this).add(0.5F, 0.5F, 0.5F), c)
                            .setOrbitAxis(Vector3.random())
                            .setOrbitRadius(0.8F + rand.nextFloat() * 0.5F)
                            .setTicksPerRotation(40 + rand.nextInt(30));
                    Effects.spawnSource(src);
                    this.effectOrbitals[i] = src;
                } else {
                    if (fxSource.canRemove() && fxSource.isRemoved()) {
                        this.effectOrbitals[i] = null;
                    }
                }
            }

            //1.16.5: MiscUtils.getRandomEntry(T[] array, Random rand) and BlockPos#add(Vec3i).
            //This tree's MiscUtils only offers the array overload as getRandomEntry(Random, T[]), and 1.20.1
            //renamed BlockPos#add(Vec3i) to BlockPos#offset(Vec3i) - same operation, arguments unchanged.
            BlockPos starlightSource = MiscUtils.getRandomEntry(rand, OFFSETS_LIQUID_STARLIGHT).offset(this.getBlockPos());

            Vector3 from = new Vector3(starlightSource).add(rand.nextFloat(), 0.85F, rand.nextFloat());
            Vector3 motion = thisPos.clone().subtract(from).normalize().multiply(0.08F);
            Color particleColor = MiscUtils.eitherOf(rand, Color.WHITE, c, c.brighter());

            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(from)
                    .setMotion(motion)
                    .alpha(VFXAlphaFunction.proximity(thisPos::clone, 2F).andThen(VFXAlphaFunction.FADE_OUT))
                    .setScaleMultiplier(0.2F + rand.nextFloat() * 0.1F)
                    .color(VFXColorFunction.constant(particleColor))
                    .setMaxAge(30 + rand.nextInt(10));

            if (rand.nextInt(80) == 0) {
                Effects.of(EffectTemplatesAS.LIGHTNING)
                        .spawn(thisPos)
                        .makeDefault(from)
                        .color(VFXColorFunction.constant(c));
            }
        } else {
            if (rand.nextBoolean()) {
                Color c = this.getCollectorType().getDisplayColor();

                Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(particlePos)
                        .setScaleMultiplier(0.2F + rand.nextFloat() * 0.1F)
                        .setAlphaMultiplier(0.8F)
                        .color(VFXColorFunction.constant(c))
                        .setMaxAge(20 + rand.nextInt(10));
            }
        }
    }

    public boolean isEnhanced() {
        return this.getCollectorType() == CollectorCrystalType.CELESTIAL_CRYSTAL && this.hasMultiblock();
    }

    public boolean isPlayerMade() {
        return this.getPlayerUUID() != null;
    }

    @Override
    public IWeakConstellation getAttunedConstellation() {
        return this.constellationType;
    }

    @Override
    public boolean setAttunedConstellation(@Nullable IWeakConstellation cst) {
        if (cst != this.constellationType) {
            markForUpdate();
        }
        this.constellationType = cst;
        return true;
    }

    @Override
    public IMinorConstellation getTraitConstellation() {
        return this.constellationTrait;
    }

    @Override
    public boolean setTraitConstellation(@Nullable IMinorConstellation cst) {
        if (cst != this.constellationTrait) {
            markForUpdate();
        }
        this.constellationTrait = cst;
        return true;
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes() {
        return crystalAttributes;
    }

    @Override
    public void setAttributes(@Nullable CrystalAttributes attributes) {
        if (this.crystalAttributes == null && attributes == null) {
            return;
        }

        // this.crystalAttributes null check is covered in .equals
        if (attributes == null ||
                !attributes.equals(this.crystalAttributes)) {
            markForUpdate();
        }
        this.crystalAttributes = attributes;
    }

    public CollectorCrystalType getCollectorType() {
        return collectorType;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void updateData(UUID playerUUID, CollectorCrystalType collectorType) {
        this.playerUUID = playerUUID;
        this.collectorType = collectorType;
        if (this.collectorType == null) {
            this.collectorType = CollectorCrystalType.ROCK_CRYSTAL;
        }

        this.markForUpdate();
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        if (this.collectorType == CollectorCrystalType.CELESTIAL_CRYSTAL) {
            return StructureTypesAS.PTYPE_ENHANCED_COLLECTOR_CRYSTAL;
        }
        return null;
    }

    @Override
    public void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.constellationType = NBTHelper.readOptional(compound, "constellationType", (nbt) -> {
            IConstellation cst = IConstellation.readFromNBT(nbt);
            if (cst instanceof IWeakConstellation) {
                return (IWeakConstellation) cst;
            }
            return null;
        });
        this.constellationTrait = NBTHelper.readOptional(compound, "constellationTrait", (nbt) -> {
            IConstellation cst = IConstellation.readFromNBT(nbt);
            if (cst instanceof IMinorConstellation) {
                return (IMinorConstellation) cst;
            }
            return null;
        });
        //1.16.5 does both of these (TileCollectorCrystal.java:260-261): the setAttributes call is what triggers the
        //markForUpdate/comparison side effect, the direct field write is the redundant-looking second half. Kept
        //as-is so the update-flag behaviour on re-read matches the reference exactly.
        setAttributes(CrystalAttributes.getCrystalAttributes(compound));
        this.crystalAttributes = CrystalAttributes.getCrystalAttributes(compound);
        this.collectorType = NBTHelper.readEnum(compound, "collectorType", CollectorCrystalType.class);
        //1.16.5: CompoundNBT#getUniqueId -> 1.20.1: CompoundTag#getUUID
        this.playerUUID = NBTHelper.readOptional(compound, "playerUUID", (nbt) -> nbt.getUUID("playerUUID"));
    }

    @Override
    public void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        if (getAttributes() != null) {
            getAttributes().store(compound);
        }
        NBTHelper.writeOptional(compound, "constellationType", this.constellationType, (nbt, cst) -> cst.writeToNBT(nbt));
        NBTHelper.writeOptional(compound, "constellationTrait", this.constellationTrait, (nbt, cst) -> cst.writeToNBT(nbt));
        NBTHelper.writeEnum(compound, "collectorType", this.collectorType);
        //1.16.5: CompoundNBT#putUniqueId -> 1.20.1: CompoundTag#putUUID
        NBTHelper.writeOptional(compound, "playerUUID", this.playerUUID, (nbt, uuid) -> nbt.putUUID("playerUUID", uuid));
    }

    @Override
    public AABB getRenderBoundingBox() {
        //1.16.5: BOX.grow(1).offset(getPos()) -> 1.20.1: AABB#inflate / AABB#move
        return BOX.inflate(1).move(getBlockPos());
    }

    @Nonnull
    @Override
    public IIndependentStarlightSource provideNewSourceNode() {
        return new IndependentCrystalSource();
    }

    @Nonnull
    @Override
    public SimpleTransmissionSourceNode provideSourceNode(BlockPos at) {
        return new SimpleTransmissionSourceNode(at);
    }
}
