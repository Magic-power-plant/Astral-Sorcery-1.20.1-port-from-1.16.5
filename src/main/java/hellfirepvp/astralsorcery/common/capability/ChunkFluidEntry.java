package hellfirepvp.astralsorcery.common.capability;

import hellfirepvp.astralsorcery.common.data.config.registry.FluidRarityRegistry;
import hellfirepvp.astralsorcery.common.data.config.registry.sets.FluidRarityEntry;
import hellfirepvp.astralsorcery.common.lib.CapabilitiesAS;
import hellfirepvp.astralsorcery.common.util.data.NBTHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class ChunkFluidEntry implements INBTSerializable<CompoundTag> {

    private FluidStack chunkFluid = FluidStack.EMPTY;
    private int mbAmount = 0;
    private boolean initialized = false;

    public boolean isInitialized() {
        return this.initialized;
    }

    public boolean isEmpty() {
        return chunkFluid.isEmpty() || mbAmount <= 0;
    }

    public void setEmpty() {
        this.chunkFluid = FluidStack.EMPTY;
        this.mbAmount = 0;
    }

    public void generate(long seed) {
        if (isInitialized()) {
            return;
        }
        Random r = new Random(seed);
        FluidRarityEntry fluidEntry = FluidRarityRegistry.INSTANCE.getRandomValue(r);
        if (fluidEntry != null) {
            this.mbAmount = fluidEntry.getRandomAmount(r);
            this.chunkFluid = new FluidStack(fluidEntry.getFluid(), 1000);
        } else {
            this.setEmpty();
        }
        this.initialized = true;
    }

    @Nonnull
    public FluidStack drain(int amount, boolean execute) {
        if (!isInitialized() || isEmpty()) {
            return new FluidStack(Fluids.WATER, amount);
        }
        int drainableAmount = Math.min(amount, this.mbAmount);
        FluidStack drained = this.chunkFluid.copy();
        drained.setAmount(drainableAmount);
        if (execute) {
            this.mbAmount -= drainableAmount;
            if (isEmpty()) {
                setEmpty();
            }
        }
        return drained;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        NBTHelper.setFluid(nbt, "chunkFluid", this.chunkFluid);
        nbt.putInt("mbAmount", this.mbAmount);
        nbt.putBoolean("initialized", this.initialized);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.chunkFluid = NBTHelper.getFluid(nbt, "chunkFluid");
        this.mbAmount = nbt.getInt("mbAmount");
        this.initialized = nbt.getBoolean("initialized");
    }

    public static class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

        private final ChunkFluidEntry entry = new ChunkFluidEntry();

        //1.16.5 (common/registry/RegistryCapabilities.java:65) allocated a fresh LazyOptional on every
        //getCapability call. ItemResonator#getFluidChunks and FountainEffectLiquid query this per chunk per
        //tick, so that is a needless allocation on a hot path, and it also means no two callers ever share an
        //instance - a consumer that registers an invalidation listener would attach it to a throwaway object.
        //One cached optional per provider (i.e. per chunk) is the Forge convention and is what the entry's
        //lifetime actually is: the provider and the ChunkFluidEntry it wraps live and die together.
        //
        //Deliberately NOT invalidated: in Forge 47.4.20 nothing ever calls LevelChunk#invalidateCaps() - the
        //method exists and delegates to the cap provider, but no caller exists in net.minecraft.server.level,
        //net.minecraft.world.level or Forge's own level hooks, so chunk capabilities are simply collected with
        //the chunk. Adding an own ChunkEvent.Unload invalidation would be behaviour neither 1.16.5 nor 1.21.1
        //has, and would risk emptying the capability while ChunkSerializer still has to write it out on the
        //save that accompanies an unload.
        private final LazyOptional<ChunkFluidEntry> entryOptional = LazyOptional.of(() -> this.entry);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == CapabilitiesAS.CHUNK_FLUID) {
                return this.entryOptional.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return entry.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            entry.deserializeNBT(nbt);
        }
    }
}
