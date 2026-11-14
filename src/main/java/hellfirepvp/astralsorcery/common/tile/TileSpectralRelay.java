/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
//1.16.5 keeps this block in common.block.tile; this tree has it at common.block (tracked as TODO_PORT[AS-PKG-01]).
import hellfirepvp.astralsorcery.common.block.BlockSpectralRelay;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.tile.altar.AltarCollectionCategory;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.ItemStackHandlerAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileSpectralRelay
 * Created by HellFirePvP
 * Date: 14.08.2019 / 06:50
 */
public class TileSpectralRelay extends TileEntityTick {

    //TODO_PORT[AS-RLY-01]: 1.16.5 backs the relay with a `TileInventory` (`new TileInventory(this, () -> 1)`,
    //1.16.5 TileSpectralRelay.java:59+69) and exposes it from getInventory(); NBT goes through
    //TileInventory#serialize/#deserialize and the capability through TileInventory#hasCapability(cap, side).
    //This tree standardised the altar/relay inventories on ItemStackHandlerAS before this slice was ported
    //(common/tile/altar/TileAltar#getInventory, and see the note in
    //common/crafting/recipe/altar/ActiveSimpleAltarRecipe.java:150), and
    //common/crafting/recipe/altar/ActiveSimpleAltarRecipe.java:181 assigns `relay.getInventory()` to an
    //`ItemStackHandlerAS` local. That file is outside this slice, so the handler type is kept here and the
    //required external change is reported instead. Functional deltas versus TileInventory: the slot accepts
    //only glass lenses (1.16.5 leaves that check to BlockSpectralRelay) and is capped at one item
    //(1.16.5 allowed a full stack in the slot, which the block never inserted).
    private final ItemStackHandlerAS inventory;

    private BlockPos altarPos;

    private BlockPos closestRelayPos;
    private float proximityMultiplier = 1F;

    public TileSpectralRelay(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.SPECTRAL_RELAY.get(), pos, state);

        this.inventory = new ItemStackHandlerAS(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.is(ItemsAS.GLASS_LENS.get());
            }

            @Override
            protected void onContentsChanged(int slot) {
                markForUpdate();
            }
        };
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide()) {
            //1.16.5: World#isAirBlock -> Level#isEmptyBlock; BlockPos#up() -> #above()
            if (!level.isEmptyBlock(getBlockPos().above())) {
                ItemStack in = getInventory().getStackInSlot(0);
                if (!in.isEmpty()) {
                    ItemStack out = ItemUtils.copyStackWithSize(in, in.getCount());
                    ItemUtils.dropItem(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), out);
                    getInventory().setStackInSlot(0, ItemStack.EMPTY);
                }
            }

            if (hasMultiblock() && hasGlassLens() && this.altarPos != null) {
                MiscUtils.executeWithChunk(level, this.altarPos, () -> {
                    TileAltar ta = MiscUtils.getTileAt(level, this.altarPos, TileAltar.class, true);
                    if (ta == null) {
                        this.updateAltarLinkState();
                    } else {
                        this.provideStarlight(ta);
                    }
                });
            }
        } else {
            if (hasMultiblock() && hasGlassLens()) {
                playStructureParticles();

                if (this.altarPos != null && doesSeeSky()) {
                    playAltarParticles();
                }
            }
        }
    }

    @Override
    protected void onFirstTick() {
        this.updateRelayProximity();
    }

    public static void cascadeRelayProximityUpdates(Level world, BlockPos pos) {
        if (world.isClientSide()) {
            return;
        }
        foreachNearbyRelay(world, pos, TileSpectralRelay::updateRelayProximity);
    }

    private void updateRelayProximity() {
        if (this.level.isClientSide() || !this.hasGlassLens()) {
            return;
        }
        this.setClosestRelayPos(null);
        BlockPos thisPos = this.getBlockPos();
        //1.16.5 lifted both positions into Vector3d (Vector3d#copy) so it could call
        //BlockPos#distanceSq(Vector3d, false) - the lower-corner-to-lower-corner squared distance. 1.20.1 dropped
        //that overload, but Vec3i#distSqr(Vec3i) computes exactly the same value, so the Vector3d hop is gone.
        foreachNearbyRelay(this.level, thisPos, relay -> {
            BlockPos relayPos = relay.getBlockPos();
            if (relayPos.equals(thisPos)) {
                return;
            }

            BlockPos otherClosestPos = relay.closestRelayPos;
            if (otherClosestPos == null || thisPos.distSqr(relayPos) < otherClosestPos.distSqr(relayPos)) {
                relay.setClosestRelayPos(thisPos);
            }
            if (this.closestRelayPos == null || relayPos.distSqr(thisPos) < this.closestRelayPos.distSqr(thisPos)) {
                this.setClosestRelayPos(relayPos);
            }
        });
    }

    private static void foreachNearbyRelay(Level world, BlockPos pos, Consumer<TileSpectralRelay> relayConsumer) {
        List<BlockPos> nearbyRelays = BlockDiscoverer.searchForBlocksAround(world, pos, 8,
                ((world1, pos1, state) -> {
                    TileSpectralRelay relay;
                    return state.getBlock() instanceof BlockSpectralRelay &&
                            (relay = MiscUtils.getTileAt(world1, pos1, TileSpectralRelay.class, false)) != null &&
                            relay.hasGlassLens() &&
                            relay.hasMultiblock();
                }));
        nearbyRelays.forEach(relayPos -> {
            TileSpectralRelay relay = MiscUtils.getTileAt(world, relayPos, TileSpectralRelay.class, false);
            if (relay != null) {
                relayConsumer.accept(relay);
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void playAltarParticles() {
        Vector3 pos = new Vector3(this).add(0.5, 0.35, 0.5);
        Vector3 target = new Vector3(this.altarPos).add(0.5, 0.5, 0.5);

        int maxAge = 30;
        maxAge *= Math.max(pos.distance(target) / 3, 1);

        EntityVisualFX vfx = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(pos)
                .alpha(VFXAlphaFunction.proximity(target::clone, 2F).andThen(VFXAlphaFunction.FADE_OUT))
                .motion(VFXMotionController.target(target::clone, 0.08F))
                .setMotion(Vector3.random().normalize().multiply(0.1F + rand.nextFloat() * 0.05F))
                .setScaleMultiplier(0.15F + rand.nextFloat() * 0.05F)
                .setMaxAge(maxAge);

        if (rand.nextBoolean()) {
            vfx.color(VFXColorFunction.WHITE);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playStructureParticles() {
        if (rand.nextBoolean()) {
            Vector3 pos = new Vector3(this).add(0.5, 0, 0.5);
            Vector3 offset = new Vector3(0, 0, 0);
            MiscUtils.applyRandomOffset(offset, rand, 1.25F);
            pos.add(offset.getX(), 0, offset.getZ());

            EntityVisualFX vfx = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                    .setGravityStrength(-0.001F)
                    .setMaxAge(30 + rand.nextInt(20));

            if (rand.nextBoolean()) {
                vfx.color(VFXColorFunction.WHITE);
            }
        }
    }

    private void provideStarlight(TileAltar ta) {
        if (this.doesSeeSky()) {
            float heightAmount = Mth.clamp((float) Math.pow(getBlockPos().getY() / 7F, 1.5F) / 60F, 0F, 1F);
            heightAmount = 0.7F + heightAmount * 0.3F;
            heightAmount *= DayTimeHelper.getCurrentDaytimeDistribution(level);
            heightAmount *= this.proximityMultiplier;
            if (heightAmount > 1E-4) {
                ta.collectStarlight(heightAmount * 45F, AltarCollectionCategory.RELAY);
            }
        }
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        if (hasGlassLens()) {
            return StructureTypesAS.PTYPE_SPECTRAL_RELAY;
        }
        return null;
    }

    @Override
    protected void notifyMultiblockStateUpdate(boolean hadMultiblockPrev, boolean hasMultiblockNow) {
        if (!hasMultiblockNow && this.altarPos != null) {
            this.altarPos = null;
        }
        if (hasMultiblockNow && this.hasGlassLens()) {
            this.updateAltarPos();
        }
    }

    public void updateAltarLinkState() {
        if (!this.hasGlassLens() || !this.hasMultiblock()) {
            this.altarPos = null;
            this.markForUpdate();
            return;
        }

        this.updateAltarPos();
    }

    private void updateAltarPos() {
        Set<BlockPos> altarPositions = BlockDiscoverer.searchForTileEntitiesAround(level, getBlockPos(), 16, tile -> tile instanceof TileAltar);

        //1.16.5 compared BlockPos#distanceSq(Vector3d.copy(getPos()), false); Vec3i#distSqr(Vec3i) is the
        //identical lower-corner squared distance in 1.20.1.
        BlockPos thisPos = getBlockPos();
        BlockPos closestAltar = null;
        for (BlockPos other : altarPositions) {
            if (closestAltar == null || other.distSqr(thisPos) < closestAltar.distSqr(thisPos)) {
                closestAltar = other;
            }
        }

        this.altarPos = closestAltar;
        this.markForUpdate();
    }

    private void setClosestRelayPos(@Nullable BlockPos closestRelayPos) {
        this.closestRelayPos = closestRelayPos;
        this.markForUpdate();

        if (this.closestRelayPos == null) {
            this.proximityMultiplier = 1F;
        } else {
            this.proximityMultiplier = Mth.clamp((float) new Vector3(this.getBlockPos()).distance(this.closestRelayPos) / 8F, 0F, 1F);
        }
    }

    public boolean hasGlassLens() {
        //1.16.5 tested `getStackInSlot(0).getItem() instanceof ItemGlassLens`. This tree has no ItemGlassLens
        //class - the lens is a plain Item registered as ItemsAS.GLASS_LENS - so the item identity is compared.
        return getInventory().getStackInSlot(0).is(ItemsAS.GLASS_LENS.get());
    }

    @Nonnull
    public ItemStackHandlerAS getInventory() {
        return inventory;
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.inventory.deserializeNBT(compound.getCompound("inventory"));
        if (compound.contains("altarPos")) {
            this.altarPos = NBTHelper.readBlockPosFromNBT(compound.getCompound("altarPos"));
        } else {
            this.altarPos = null;
        }
        if (compound.contains("closestRelayPos")) {
            this.setClosestRelayPos(NBTHelper.readBlockPosFromNBT(compound.getCompound("closestRelayPos")));
        } else {
            this.setClosestRelayPos(null);
        }
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        compound.put("inventory", this.inventory.serializeNBT());
        if (this.altarPos != null) {
            compound.put("altarPos", NBTHelper.writeBlockPosToNBT(this.altarPos, new CompoundTag()));
        }
        if (this.closestRelayPos != null) {
            compound.put("closestRelayPos", NBTHelper.writeBlockPosToNBT(this.closestRelayPos, new CompoundTag()));
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        //1.16.5 asked TileInventory#hasCapability(cap, side) (which also honoured the inventory's side filter,
        //empty here = all sides) and handed out TileInventory#getCapability().
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return getCachedCapability(new CapabilityKey(cap, side), () -> inventory).cast();
        }
        return super.getCapability(cap, side);
    }
}
