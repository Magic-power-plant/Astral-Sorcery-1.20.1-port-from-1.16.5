/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active;

import hellfirepvp.astralsorcery.client.effect.EntityComplexFX;
import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalCrystalAttunement;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.util.sound.FadeLoopSound;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttuneCrystalRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.AdvancementsAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.blockentity.AttunementAltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActiveCrystalAttunementRecipe
 * Created by HellFirePvP
 * Date: 05.12.2019 / 06:33
 */
public class ActiveCrystalAttunementRecipe extends AttunementRecipe.Active<AttuneCrystalRecipe> {

    private static final int DURATION_CRYSTAL_ATTUNEMENT = 500;

    private IConstellation constellation;
    private int entityId;

    //The item attunement sound, FadeLoopSound
    private Object itemAttuneSound;

    private Object innerOrbital1;
    private Object attunementFlare;

    public ActiveCrystalAttunementRecipe(AttuneCrystalRecipe recipe, IConstellation constellation, int crystalEntityId) {
        super(recipe);
        this.constellation = constellation;
        this.entityId = crystalEntityId;
    }

    public ActiveCrystalAttunementRecipe(AttuneCrystalRecipe recipe, CompoundTag nbt) {
        super(recipe);
        this.readFromNBT(nbt);
    }

    @Override
    public boolean matches(AttunementAltarBlockEntity altar) {
        if (!super.matches(altar)) {
            return false;
        }
        Entity entity;
        return (entity = altar.getLevel().getEntity(this.entityId)) != null &&
                entity.isAlive() &&
                entity instanceof ItemEntity &&
                this.constellation.equals(altar.getActiveConstellation()) &&
                AttuneCrystalRecipe.isApplicableCrystal((ItemEntity) entity, altar.getActiveConstellation());
    }

    @Override
    public void startCrafting(AttunementAltarBlockEntity altar) {

    }

    @Override
    public void stopCrafting(AttunementAltarBlockEntity altar) {

    }

    @Override
    public void finishRecipe(AttunementAltarBlockEntity altar) {
        ItemEntity crystal = this.getEntity(altar.getLevel());
        if (crystal != null) {
            ItemStack stack = crystal.getItem();
            if (!(stack.getItem() instanceof ConstellationItem) && stack.getItem() instanceof ItemCrystalBase) {
                CompoundTag tag = stack.getTag();
                stack = new ItemStack(((ItemCrystalBase) stack.getItem()).getTunedItemVariant(), stack.getCount());
                stack.setTag(tag);
            }
            if (stack.getItem() instanceof ConstellationItem) {
                IWeakConstellation attuned = ((ConstellationItem) stack.getItem()).getAttunedConstellation(stack);
                IMinorConstellation trait = ((ConstellationItem) stack.getItem()).getTraitConstellation(stack);
                if (attuned == null) {
                    if (altar.getActiveConstellation() instanceof IWeakConstellation) {
                        ((ConstellationItem) stack.getItem()).setAttunedConstellation(stack, (IWeakConstellation) altar.getActiveConstellation());
                    }
                } else if (trait == null) {
                    if (altar.getActiveConstellation() instanceof IMinorConstellation) {
                        ((ConstellationItem) stack.getItem()).setTraitConstellation(stack, (IMinorConstellation) altar.getActiveConstellation());
                    }
                }
                crystal.setItem(stack);

                // 1.16.5 read the raw thrower UUID via getThrowerId() and resolved it through World#getPlayerByUuid.
                // 1.20.1 removed the UUID getter (the 'thrower' field is private, see official mappings for ItemEntity);
                // ItemEntity#getOwner() is the only accessor and already resolves the thrower against the ServerLevel,
                // which is where finishRecipe runs.
                Entity thrower = crystal.getOwner();
                if (thrower instanceof ServerPlayer) {
                    AdvancementsAS.ATTUNE_CRYSTAL.trigger((ServerPlayer) thrower, altar.getActiveConstellation());
                }
            }
        }
    }

    @Override
    public void doTick(LogicalSide side, AttunementAltarBlockEntity altar) {
        ItemEntity crystal = this.getEntity(altar.getLevel());
        if (crystal == null) {
            return;
        }

        Vector3 crystalHoverPos = new Vector3(altar).add(0.5, 1.4, 0.5);
        crystal.setPos(crystalHoverPos.getX(), crystalHoverPos.getY(), crystalHoverPos.getZ());
        // 1.16.5 prevPosX/prevPosY/prevPosZ are xo/yo/zo in official mappings
        crystal.xo = crystalHoverPos.getX();
        crystal.yo = crystalHoverPos.getY();
        crystal.zo = crystalHoverPos.getZ();
        crystal.setDeltaMovement(0, 0, 0);

        if (side.isClient()) {
            doClientTick(altar);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doClientTick(AttunementAltarBlockEntity altar) {
        Predicate<PositionedLoopSound> activeTest = s ->
                !altar.canPlayConstellationActiveEffects() ||
                        altar.getActiveRecipe() != this;

        if (this.itemAttuneSound == null || ((FadeLoopSound) this.itemAttuneSound).hasStoppedPlaying()) {
            this.itemAttuneSound = SoundHelper.playSoundLoopFadeInClient(SoundsAS.ATTUNEMENT_ATLAR_ITEM_LOOP,
                    new Vector3(altar).add(0.5, 1, 0.5), 1F, 1F, false, activeTest)
                    .setFadeInTicks(20)
                    .setFadeOutTicks(20);
        }

        if (this.getTick() == 0) {
            SoundHelper.playSoundClientWorld(SoundsAS.ATTUNEMENT_ATLAR_ITEM_START, altar.getBlockPos(), 1F, 1F);
        }

        if (this.getTick() >= 80) {
            if (attunementFlare == null || ((EntityComplexFX) attunementFlare).isRemoved()) {
                attunementFlare = Effects.of(EffectTemplatesAS.FACING_SPRITE)
                        .spawn(new Vector3(altar).add(0.5, 1.75, 0.5))
                        .setSprite(SpritesAS.SPR_ATTUNEMENT_FLARE)
                        .setScaleMultiplier(2.5F)
                        .refresh(fx -> altar.canPlayConstellationActiveEffects() &&
                                altar.getActiveRecipe() == this);
            }
        }

        Vector3 altarPos = new Vector3(altar).add(0.5, 0, 0.5);
        if (innerOrbital1 == null) {
            innerOrbital1 = Effects.spawnSource(new FXOrbitalCrystalAttunement(altarPos.clone(), altarPos.clone().addY(1.75), this.constellation))
                    .setOrbitRadius(3)
                    .setBranches(4)
                    .setOrbitAxis(Vector3.RotAxis.Y_AXIS)
                    .setTicksPerRotation(200)
                    .refresh(RefreshFunction.blockEntityExistsAnd(altar,
                            (blockEntity, effect) -> blockEntity.canPlayConstellationActiveEffects() &&
                                    blockEntity.getActiveRecipe() == this));
        }

        VFXColorFunction<?> beamColor = VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE);
        if (getTick() >= 80 && getTick() % 40 == 0) {
            for (BlockPos pos : altar.getConstellationPositions(this.constellation)) {
                Vector3 from = new Vector3(pos).add(0.5, 0, 0.5);
                MiscUtils.applyRandomOffset(from, rand, 0.1F);
                Effects.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(from)
                        .setup(from.clone().addY(6), 1.2, 1.2)
                        .setAlphaMultiplier(0.8F)
                        .color(beamColor)
                        .setMaxAge(60);
            }
        }

        float total = DURATION_CRYSTAL_ATTUNEMENT;
        float percCycle = (float) (((getTick() % total) / total) * 2 * Math.PI);
        int parts = getTick() % 50 == 0 ? 180 : 6;
        Vector3 center = new Vector3(altar).add(0.5, 0.1, 0.5);
        float angleSwirl = 120F;
        float dst = 4.5F;

        for (int i = 0; i < parts; i++) {
            Vector3 v = Vector3.RotAxis.X_AXIS.clone();
            float originalAngle = (((float) i) / ((float) parts)) * 360F;
            // 1.16.5 MathHelper is Mth in official mappings
            double angle = originalAngle + (Mth.sin(percCycle) * angleSwirl);
            v.rotate(-Math.toRadians(angle), Vector3.RotAxis.Y_AXIS).normalize().multiply(dst);
            Vector3 pos = center.clone();
            Vector3 mot = center.clone().subtract(pos.clone().add(v)).normalize().multiply(0.14);

            int age = 20 + rand.nextInt(30);
            float size = 0.2F + rand.nextFloat() * 0.7F;
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .setScaleMultiplier(size)
                    .setMotion(mot)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(age);

            if (rand.nextInt(6) == 0) {
                Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .setScaleMultiplier(size * 1.4F)
                        .setMotion(mot)
                        .color(VFXColorFunction.constant(this.constellation.getConstellationColor()))
                        .setGravityStrength(-0.0004F + rand.nextFloat() * -0.00015F)
                        .setMaxAge(age + 30);
            }
        }

        double scale = 7.0D;
        double edgeScale = (scale * 2 + 1);
        for (int i = 0; i < 7; i++) {
            Vector3 offset = new Vector3(altar).add(-scale, 0.1, -scale);
            if (rand.nextBoolean()) {
                offset.add(edgeScale * (rand.nextBoolean() ? 1 : 0), 0, rand.nextFloat() * edgeScale);
            } else {
                offset.add(rand.nextFloat() * edgeScale, 0, edgeScale * (rand.nextBoolean() ? 1 : 0));
            }
            FXFacingParticle particle = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setGravityStrength(-0.0002F + rand.nextFloat() * -0.0001F)
                    .setScaleMultiplier(0.3F + rand.nextFloat() * 0.15F)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(40 + rand.nextInt(10));
            if (rand.nextBoolean()) {
                particle.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
            }
        }

        if (this.getTick() >= 200) {
            for (int i = 0; i < 3; i++) {
                Vector3 at = new Vector3(altar).add(0.5, 0, 0.5);
                at.addX(rand.nextFloat() * 7F * (rand.nextBoolean() ? 1 : -1));
                at.addZ(rand.nextFloat() * 7F * (rand.nextBoolean() ? 1 : -1));

                FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .setAlphaMultiplier(0.75F)
                        .alpha(VFXAlphaFunction.FADE_OUT)
                        .setGravityStrength(-0.001F + rand.nextFloat() * -0.0005F)
                        .color(VFXColorFunction.WHITE)
                        .setScaleMultiplier(0.3F + rand.nextFloat() * 0.1F)
                        .setMaxAge(20 + rand.nextInt(10));

                if (rand.nextBoolean()) {
                    p.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
                }
                if (getTick() >= 400) {
                    p.setScaleMultiplier(0.3F + rand.nextFloat() * 0.15F);
                }
            }
        }

        if (getTick() >= 460) {
            if (getTick() % 5 == 0) {
                Vector3 from = new Vector3(altar).add(0.5, 0, 0.5);
                MiscUtils.applyRandomOffset(from, rand, 0.25F);
                Effects.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(from)
                        .setup(from.clone().addY(8), 2.4, 2)
                        .setAlphaMultiplier(0.8F)
                        .setMaxAge(30 + rand.nextInt(15));
            }
        }

        if (getTick() >= (DURATION_CRYSTAL_ATTUNEMENT - 10)) {
            for (int i = 0; i < 25; i++) {
                Vector3 at = new Vector3(altar)
                        .add(0.5, 0, 0.5)
                        .addY(rand.nextFloat() * 0.5 + rand.nextFloat() * 0.5);

                FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .color(VFXColorFunction.WHITE)
                        .setMotion(Vector3.random().setY(0).normalize().multiply(0.025 + rand.nextFloat() * 0.075))
                        .setAlphaMultiplier(0.75F)
                        .setScaleMultiplier(0.25F + rand.nextFloat() * 0.15F)
                        .alpha(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(60 + rand.nextInt(40));

                if (rand.nextBoolean()) {
                    p.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
                }
            }
        }
    }

    @Override
    public boolean isFinished(AttunementAltarBlockEntity altar) {
        return this.getTick() >= DURATION_CRYSTAL_ATTUNEMENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void stopEffects(AttunementAltarBlockEntity altar) {
        if (isFinished(altar)) {
            SoundHelper.playSoundClientWorld(SoundsAS.ATTUNEMENT_ATLAR_ITEM_FINISH, altar.getBlockPos().above(), 1F, 1F);
        }
        if (innerOrbital1 != null) {
            ((EntityComplexFX) innerOrbital1).requestRemoval();
        }
        if (attunementFlare != null) {
            ((EntityComplexFX) attunementFlare).requestRemoval();
        }
    }

    @Nullable
    private ItemEntity getEntity(Level world) {
        Entity entity = world.getEntity(this.entityId);
        if (entity != null && entity.isAlive() && entity instanceof ItemEntity) {
            return (ItemEntity) entity;
        }
        return null;
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);

        nbt.putString("constellation", this.constellation.getRegistryName().toString());
        nbt.putInt("entityId", this.entityId);
    }

    @Override
    protected void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);

        this.constellation = RegistriesAS.REGISTRY_CONSTELLATIONS.getValue(new ResourceLocation(nbt.getString("constellation")));
        this.entityId = nbt.getInt("entityId");
    }
}
