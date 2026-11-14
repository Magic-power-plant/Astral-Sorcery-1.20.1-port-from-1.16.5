/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.refraction;

import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.effect.vfx.FXSpritePlane;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.engraving.EngravedStarMap;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.item.ItemInfusedGlass;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.base.TickableBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import hellfirepvp.astralsorcery.common.blockentity.capability.NamedInventoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RefractionTableBlockEntity
 * Created by HellFirePvP
 * Date: 26.04.2020 / 20:18
 */
public class RefractionTableBlockEntity extends TickableBlockEntity implements NamedInventoryBlockEntity {

    private static final float RUN_TIME = 10 * 20F;

    private int runTick = 0;

    private ItemStack glassStack = ItemStack.EMPTY;
    //Either have parchment or an input. not both.
    private int parchmentCount = 0;
    private ItemStack inputStack = ItemStack.EMPTY;

    private Object effectHalo;

    public RefractionTableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.REFRACTION_TABLE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getLevel().isClientSide()) {
            playEngravingEffects();
        } else {
            if (DayTimeHelper.isNight(getLevel()) &&
                    this.doesSeeSky() &&
                    isValidGlassStack(this.getGlassStack())) {

                EngravedStarMap starMap = ItemInfusedGlass.getEngraving(this.getGlassStack());
                if (starMap != null &&
                        !this.hasParchment() &&
                        !this.getInputStack().isEmpty() &&
                        starMap.canAffect(this.getInputStack())) {
                    runTick++;
                    if (runTick > RUN_TIME) {
                        this.setInputStack(starMap.applyEffects(this.getInputStack()));
                        ItemStack glassStack = this.getGlassStack();
                        //1.16.5: ItemStack#attemptDamageItem(int, Random, ServerPlayerEntity) -> #hurt(int, RandomSource, ServerPlayer).
                        //The tile's shared java.util.Random is no longer accepted, so it seeds a RandomSource instead
                        //(same house style as CropHelper#growCrop) to keep drawing from the one tile-side sequence.
                        if (glassStack.hurt(1, RandomSource.create(rand.nextLong()), null)) {
                            glassStack.shrink(1);
                            this.setGlassStack(glassStack);
                            SoundHelper.playSoundAround(SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, this.getLevel(), this.getBlockPos(), rand.nextFloat() * 0.5F + 1F, rand.nextFloat() * 0.2F + 0.8F);
                        }
                        this.resetWorkTick();
                    }
                    markForUpdate();
                } else {
                    this.resetWorkTick();
                }
            } else {
                this.resetWorkTick();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playEngravingEffects() {
        if (this.runTick <= 0) {
            return;
        }

        if (this.effectHalo != null && ((FXSpritePlane) this.effectHalo).isRemoved()) {
            Effects.refresh((FXSpritePlane) this.effectHalo, EffectTemplatesAS.TEXTURE_SPRITE);
        }
        if (this.effectHalo == null) {
            this.effectHalo = Effects.of(EffectTemplatesAS.TEXTURE_SPRITE)
                    .spawn(new Vector3(this).add(0.5, 0.8, 0.5))
                    .setSprite(SpritesAS.SPR_HALO_INFUSION)
                    .setAxis(Vector3.RotAxis.Y_AXIS)
                    .setNoRotation(0)
                    .setScaleMultiplier(0.8F)
                    .setAlphaMultiplier(0.8F)
                    .alpha(((fx, alpha, pTicks) -> Mth.clamp(alpha * getRunProgress(), 0F, 1F)))
                    .refresh(RefreshFunction.blockEntityExistsAnd(this, (thisBlockEntity, fx) -> thisBlockEntity.getRunProgress() > 0));
        }

        Vector3 offset = new Vector3(-5.0 / 16.0, 1.505, -3.0 / 16.0);
        int random = rand.nextInt(ColorsAS.REFRACTION_TABLE_COLORS.length);
        if (random >= ColorsAS.REFRACTION_TABLE_COLORS.length / 2) { //0-5 is left, 6-11 is right
            offset.addX(24.0 / 16.0);
        }
        offset.addZ((random % (ColorsAS.REFRACTION_TABLE_COLORS.length / 2)) * (4.0 / 16.0));
        //1.16.5 read the TileEntity's protected 'pos' field directly; in 1.20.1 that field is 'worldPosition'.
        offset.add(rand.nextFloat() * 0.1, 0, rand.nextFloat() * 0.1).add(this.getBlockPos());
        Color color = ColorsAS.REFRACTION_TABLE_COLORS[random];

        Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(offset)
                .setGravityStrength(-0.002F)
                .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.constant(color))
                .setMaxAge(30 + rand.nextInt(30));

        if (rand.nextFloat() < (getRunProgress() * 2F)) {
            Vector3 target = new Vector3(this).add(0.5, 0.9, 0.5);

            Effects.of(EffectTemplatesAS.LIGHTNING)
                    .spawn(offset)
                    .makeDefault(target)
                    .color(VFXColorFunction.constant(color));

            FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                    .alpha(VFXAlphaFunction.proximity(target::clone, 1F))
                    .color(VFXColorFunction.constant(color))
                    .setMaxAge(45);

            Vector3 mov = target.clone().subtract(offset).normalize().multiply(0.05 * rand.nextFloat());
            p.setMotion(mov);
        }

        if (rand.nextInt(3) == 0) {
            Effects.of(EffectTemplatesAS.LIGHTBEAM)
                    .spawn(offset)
                    .setup(offset.clone().addY(0.56F + rand.nextFloat() * 0.2F), 0.25F, 0.25F)
                    .color(VFXColorFunction.constant(color));
        }

        if (rand.nextInt(4) == 0) {
            Color beamColor = MiscUtils.eitherOf(rand,
                    ColorsAS.CONSTELLATION_TYPE_MAJOR,
                    ColorsAS.CONSTELLATION_TYPE_WEAK,
                    ColorsAS.CONSTELLATION_TYPE_MINOR);

            Vector3 beamOffset = new Vector3(this).add(0.1 + rand.nextFloat() * 0.8, 0.8, 0.1 + rand.nextFloat() * 0.8F);
            Effects.of(EffectTemplatesAS.LIGHTBEAM)
                    .spawn(beamOffset)
                    .setup(beamOffset.clone().addY(1 + rand.nextFloat() * 0.5), 0.5F, 0.5F)
                    .color(VFXColorFunction.constant(beamColor))
                    .setMaxAge(25 + rand.nextInt(5));
        }
    }

    private void resetWorkTick() {
        if (this.runTick > 0) {
            this.runTick = 0;
            this.markForUpdate();
        }
    }

    public int addParchment(int toAdd) {
        if (this.inputStack.isEmpty()) {
            int overflow = Math.max(this.parchmentCount + toAdd - 64, 0);
            int addable = toAdd - overflow;
            this.parchmentCount += addable;
            this.markForUpdate();
            return overflow;
        }
        return toAdd;
    }

    public int getParchmentCount() {
        return this.parchmentCount;
    }

    public boolean hasParchment() {
        return this.parchmentCount > 0;
    }

    public void engraveGlass(List<DrawnConstellation> constellations) {
        if (this.hasParchment() && this.hasUnengravedGlass()) {
            this.parchmentCount--;
            ItemInfusedGlass.setEngraving(this.getGlassStack(), EngravedStarMap.buildStarMap(this.getLevel(), constellations));
            this.markForUpdate();
        }
    }

    @Nonnull
    public ItemStack setInputStack(@Nonnull ItemStack inputStack) {
        ItemStack prevInput = this.inputStack.copy();
        if (this.parchmentCount > 0) {
            prevInput = new ItemStack(ItemsAS.PARCHMENT.get(), this.parchmentCount);
            this.parchmentCount = 0;
        }
        this.inputStack = inputStack.copy();
        this.markForUpdate();
        return prevInput;
    }

    @Nonnull
    public ItemStack getInputStack() {
        return this.hasParchment() ? new ItemStack(ItemsAS.PARCHMENT.get(), this.getParchmentCount()) : this.inputStack.copy();
    }

    public static boolean isValidGlassStack(@Nonnull ItemStack glassStack) {
        return !glassStack.isEmpty() && glassStack.getItem() instanceof ItemInfusedGlass;
    }

    @Nonnull
    public ItemStack setGlassStack(@Nonnull ItemStack glassStack) {
        if (!glassStack.isEmpty() && !isValidGlassStack(glassStack)) {
            return ItemStack.EMPTY;
        }
        ItemStack prevStack = this.glassStack;
        this.glassStack = glassStack;
        this.markForUpdate();
        return prevStack;
    }

    @Nonnull
    public ItemStack getGlassStack() {
        return glassStack;
    }

    public boolean hasUnengravedGlass() {
        return isValidGlassStack(this.getGlassStack()) &&
                ItemInfusedGlass.getEngraving(this.getGlassStack()) == null;
    }

    public float getRunProgress() {
        return Mth.clamp(this.runTick / RUN_TIME, 0F, 1F);
    }

    public void dropContents() {
        Vector3 at = new Vector3(this).add(0.5, 0.5, 0.5);
        if (!this.getGlassStack().isEmpty()) {
            ItemUtils.dropItemNaturally(this.getLevel(), at.getX(), at.getY(), at.getZ(), this.getGlassStack());
            this.setGlassStack(ItemStack.EMPTY);
        }
        if (!this.getInputStack().isEmpty()) {
            ItemUtils.dropItemNaturally(this.getLevel(), at.getX(), at.getY(), at.getZ(), this.getInputStack());
            this.setInputStack(ItemStack.EMPTY);
        }
        this.markForUpdate();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.astralsorcery.refraction_table");
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.runTick = compound.getInt("runTick");

        this.parchmentCount = compound.getInt("parchmentCount");
        this.inputStack = NBTHelper.getStack(compound, "inputStack");
        this.glassStack = NBTHelper.getStack(compound, "glassStack");
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        compound.putInt("runTick", this.runTick);

        compound.putInt("parchmentCount", this.parchmentCount);
        NBTHelper.setStack(compound, "inputStack", this.inputStack);
        NBTHelper.setStack(compound, "glassStack", this.glassStack);
    }
}
