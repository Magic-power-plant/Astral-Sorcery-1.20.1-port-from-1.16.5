package hellfirepvp.astralsorcery.common.blockentity.starlight;

import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefactionContext;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.api.StarlightReceiverBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightReceiverWell;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.blockentity.capability.FluidTankAccess;
import hellfirepvp.astralsorcery.common.blockentity.fluid.PrecisionSingleFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//1.16.5 declares `TileWell extends TileReceiverBase<StarlightReceiverWell>` (1.16.5 TileWell.java:60). This tree
//had it extend TileEntitySynchronized instead, which dropped the well out of the starlight network entirely:
//TileNetwork#tick is what calls TransmissionNetworkHelper.informNetworkTilePlacement, and IStarlightReceiver is
//what makes provideEndpoint(BlockPos) exist. Without them StarlightReceiverWell - which is fully implemented and
//registered in TransmissionClassRegistry - had no producer at all, so a placed well never joined the network and
//receiveStarlight(double) was never called by anything. The well could only ever accumulate its own passive
//collectStarlight() gain, never starlight relayed from a collector crystal.
public class WellBlockEntity extends StarlightReceiverBlockEntity<StarlightReceiverWell> {

    public static final String TAG_STARLIGHT_BUFFER = "starlightBuffer";
    public static final String TAG_CATALYST = "catalyst";
    private static final String TAG_TANK = "tank";
    private static final int TANK_SIZE = 2 * FluidType.BUCKET_VOLUME;

    private final PrecisionSingleFluidTank tank;
    private final FluidTankAccess tankAccess;
    private final ItemStackHandler catalystInventory;
    private WellLiquefaction runningRecipe = null;
    private double starlightBuffer = 0;
    private float posDistribution = -1;

    public WellBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.WELL.get(), pos, state);
        this.tank = new PrecisionSingleFluidTank(TANK_SIZE);
        this.tank.setAllowInput(false);
        this.tank.addUpdateFunction(this::markForUpdate);
        this.tankAccess = new FluidTankAccess();
        this.tankAccess.putTank(0, tank, Direction.DOWN);
        this.catalystInventory = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return level == null || WellLiquefaction.findRecipe(level, new WellLiquefactionContext(stack)) != null;
            }

            @Override
            protected void onContentsChanged(int slot) {
                runningRecipe = null;
                markForUpdate();
            }
        };
    }

    @Override
    public void tick() {
        //TileNetwork#tick registers this tile with the starlight network on its first server tick; without the
        //super call the well is in the network's class registry but never announced to it.
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        if (doesSeeSky()) {
            collectStarlight();
        }

        if (!getCatalyst().isEmpty()) {
            tickCatalyst();
        }
        starlightBuffer = 0;
    }

    private void tickCatalyst() {
        if (level == null) {
            return;
        }
        if (!level.isEmptyBlock(getBlockPos().above())) {
            breakCatalyst();
            return;
        }
        if (runningRecipe == null) {
            runningRecipe = WellLiquefaction.findRecipe(level, new WellLiquefactionContext(this));
        }
        if (runningRecipe == null) {
            breakCatalyst();
            return;
        }

        //1.16.5 TileWell.java:112-128 scales both the fluid gain and the shatter resistance by the catalyst's
        //total crystal tier. Dropping it made every catalyst behave like a tier-1 crystal: a perfect rock crystal
        //produced the same output as a plain one, and shattered just as often.
        int statMultiplier = 1;
        ItemStack catalyst = getCatalyst();
        if (catalyst.getItem() instanceof CrystalAttributeItem attributeItem) {
            CrystalAttributes attributes = attributeItem.getAttributes(catalyst);
            if (attributes != null) {
                statMultiplier = Math.max(1, attributes.getTotalTierLevel());
            }
        }

        double gain = Math.sqrt(starlightBuffer) * (statMultiplier * runningRecipe.getProductionMultiplier());
        if (gain > 0 && tank.getFluidAmount() <= TANK_SIZE) {
            fillAndDiscardRest(runningRecipe, gain);
        }

        //A higher multiplier widens the bound, so a better crystal shatters less often - as in 1.16.5.
        int shatterBound = Math.max(1, 1 + (int) (1000 * (statMultiplier * runningRecipe.getShatterMultiplier())));
        if (level.random.nextInt(shatterBound) == 0) {
            breakCatalyst();
        }
    }

    private void fillAndDiscardRest(WellLiquefaction recipe, double gain) {
        Fluid produced = recipe.getFluidOutput();
        if (produced == Fluids.EMPTY) {
            return;
        }
        if (tank.getFluidAmount() < 10) {
            tank.setFluid(produced);
        }
        if (tank.getFluid().isEmpty()) {
            tank.setFluid(produced);
        } else if (!produced.equals(tank.getFluid().getFluid())) {
            return;
        }
        tank.addAmount(gain);
    }

    public void breakCatalyst() {
        catalystInventory.setStackInSlot(0, ItemStack.EMPTY);
        runningRecipe = null;
        if (level != null) {
            level.playSound(null, getBlockPos(), SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1F, 1F);
        }
        markForUpdate();
    }

    private void collectStarlight() {
        if (level == null) return;

        float dayDistribution = getDaytimeDistribution();
        int yLevel = getBlockPos().getY();
        float heightFactor = yLevel > 120 ? 1F : yLevel / 120F;

        if (posDistribution < 0) {
            posDistribution = 0.3F;
        }
        posDistribution *= 0.9F;

        float skyExposure = calculateSkyExposure();
        posDistribution += skyExposure * 0.1F;

        starlightBuffer += Math.max(0.0001, dayDistribution * heightFactor * (1 + 1.2 * posDistribution));
        markForUpdate();
    }

    private float calculateSkyExposure() {
        if (level == null) return 0.3F;
        BlockPos pos = getBlockPos();
        int openSky = 0;
        int checks = 8;

        for (int i = 0; i < checks; i++) {
            double angle = (i / (double) checks) * Math.PI * 2;
            int dx = (int) Math.cos(angle);
            int dz = (int) Math.sin(angle);
            BlockPos checkPos = pos.offset(dx * 3, 5, dz * 3);
            if (level.canSeeSky(checkPos)) {
                openSky++;
            }
        }
        return openSky / (float) checks;
    }

    private float getDaytimeDistribution() {
        if (level == null) return 0.5F;
        long gameTime = level.getGameTime();
        int hour = (int) (gameTime / 1000 % 24);
        if (hour >= 22 || hour <= 4) {
            return 1.0F;
        } else if (hour >= 20 || hour <= 6) {
            return 0.7F;
        } else if (hour >= 8 && hour <= 18) {
            return 0.2F;
        }
        return 0.5F;
    }

    //Deliberately overrides TileEntityTick#doesSeeSky with a direct check. The base version caches the answer,
    //re-samples only every 20 ticks and syncs it to the client; the well only needs the server-side boolean each
    //tick. Kept as-is to limit this slice to the starlight-network regression - if the cached/synced behaviour
    //turns out to matter for the well's client rendering, deleting this override restores the reference path.
    @Override
    public boolean doesSeeSky() {
        return level != null && level.hasChunkAt(getBlockPos()) && MiscUtils.canSeeSkyDirectly(level, getBlockPos());
    }

    public void receiveStarlight(double amount) {
        this.starlightBuffer += amount;
        markForUpdate();
    }

    //1.16.5 TileWell.java:255-258. This is the producer StarlightReceiverWell never had: the network calls it when
    //the tile is announced, and the returned node is what later routes onStarlightReceive back to receiveStarlight.
    @Nonnull
    @Override
    public StarlightReceiverWell provideEndpoint(BlockPos at) {
        return new StarlightReceiverWell(at);
    }

    @Nonnull
    public PrecisionSingleFluidTank getTank() {
        return tank;
    }

    @Nonnull
    public ItemStackHandler getInventory() {
        return catalystInventory;
    }

    @Nonnull
    public ItemStack getCatalyst() {
        return catalystInventory.getStackInSlot(0);
    }

    public void setCatalyst(@Nonnull ItemStack stack) {
        this.catalystInventory.setStackInSlot(0, stack);
        this.runningRecipe = null;
        markForUpdate();
    }

    public double getStarlightBuffer() {
        return starlightBuffer;
    }

    public boolean canAcceptCatalyst(ItemStack stack) {
        return !stack.isEmpty() && level != null && catalystInventory.isItemValid(0, stack) && getCatalyst().isEmpty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (tankAccess.hasCapability(capability, side)) {
            return getCachedCapability(new CapabilityKey(capability, side), () -> tankAccess.getCapability(side).orElseThrow(() -> new IllegalStateException("Capability availability changed during lookup"))).cast();
        }
        if (ForgeCapabilities.ITEM_HANDLER == capability && (side == null || side == Direction.DOWN)) {
            return getCachedCapability(new CapabilityKey(capability, side), () -> catalystInventory).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void writeCustomNBT(CompoundTag tag) {
        //TileEntityTick writes ticksExisted/doesSeeSky/hasMultiblock here; without the super call those three
        //stopped round-tripping the moment this tile gained that base class.
        super.writeCustomNBT(tag);
        tag.putDouble(TAG_STARLIGHT_BUFFER, starlightBuffer);
        tag.putFloat("posDistribution", posDistribution);
        tag.put(TAG_TANK, tank.writeNBT());
        tag.put(TAG_CATALYST, catalystInventory.serializeNBT());
    }

    @Override
    protected void readCustomNBT(CompoundTag tag) {
        super.readCustomNBT(tag);
        starlightBuffer = tag.getDouble(TAG_STARLIGHT_BUFFER);
        posDistribution = tag.getFloat("posDistribution");
        if (tag.contains(TAG_TANK)) {
            tank.readNBT(tag.getCompound(TAG_TANK));
        }
        if (tag.contains(TAG_CATALYST)) {
            catalystInventory.deserializeNBT(tag.getCompound(TAG_CATALYST));
        } else {
            catalystInventory.setStackInSlot(0, ItemStack.EMPTY);
        }
        runningRecipe = null;
    }
}
