package hellfirepvp.astralsorcery.common.constellation.effect.base;

import com.mojang.datafixers.util.Either;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualLink;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockPositionGenerator;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockRandomPositionGenerator;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CEffectAbstractList<T extends CEffectAbstractList.ListEntry> extends ConstellationEffect {

    protected boolean isLinkedRitual = false;
    private boolean excludesRitual = false;
    private boolean excludeRitualColumn = false;

    protected final BlockPredicate verifier;
    protected final int maxAmount;
    private final BlockPositionGenerator positionStrategy;
    private final List<T> elements = new ArrayList<>();

    protected CEffectAbstractList(@Nonnull ILocatable origin, @Nonnull IWeakConstellation cst, int maxAmount, BlockPredicate verifier) {
        super(origin, cst);
        this.maxAmount = maxAmount;
        this.verifier = verifier;
        this.positionStrategy = this.createPositionStrategy();
    }

    protected void excludeRitualPositions() {
        this.setChunkNeedsToBeLoaded();
        if (!this.excludesRitual) {
            this.excludesRitual = true;
            this.positionStrategy.andFilter(this.createExcludeRitualPredicate());
        }
    }

    protected void excludeRitualColumn() {
        this.setChunkNeedsToBeLoaded();
        if (!this.excludeRitualColumn) {
            this.excludeRitualColumn = true;
            this.positionStrategy.andFilter(this.createExcludeRitualColumnPredicate());
        }
    }

    protected void selectSphericalPositions() {
        this.positionStrategy.andFilter((pos, radius) -> {
            double dst = new Vector3(this.getPos().getLocationPos()).add(0.5, 0.5, 0.5)
                    .distanceSquared(new Vector3(pos).add(this.getPos().getLocationPos()).add(0.5, 0.5, 0.5));
            return dst <= radius * radius;
        });
    }

    @Nullable
    public abstract T recreateElement(CompoundTag tag, BlockPos pos);

    @Nullable
    public abstract T createElement(Level world, BlockPos pos);

    @Nonnull
    protected BlockPositionGenerator createPositionStrategy() {
        return new BlockRandomPositionGenerator();
    }

    @Nonnull
    protected BlockPositionGenerator selectPositionStrategy(BlockPositionGenerator defaultGenerator, ConstellationEffectProperties properties) {
        return defaultGenerator;
    }

    private java.util.function.Predicate<BlockPos> createExcludeRitualPredicate() {
        var structure = StructureTypesAS.PTYPE_RITUAL_PEDESTAL.getStructure();
        return pos ->
                !pos.equals(BlockPos.ZERO) &&
                        this.isLinkedRitual || (
                                !pos.equals(TileRitualPedestal.RITUAL_ANCHOR_OFFEST) && (
                                        pos.getY() >= 3 ||
                                                (!structure.hasBlockAt(pos) &&
                                                        !structure.hasBlockAt(pos.below()) &&
                                                        !structure.hasBlockAt(pos.below(2)) &&
                                                        !structure.hasBlockAt(pos.below(3)) &&
                                                        !structure.hasBlockAt(pos.below(4)))));
    }

    private java.util.function.Predicate<BlockPos> createExcludeRitualColumnPredicate() {
        var structure = StructureTypesAS.PTYPE_RITUAL_PEDESTAL.getStructure();
        return pos -> (this.isLinkedRitual && !(pos.getX() == 0 && pos.getZ() == 0)) ||
                (!this.isLinkedRitual && !structure.hasBlockAt(new BlockPos(pos.getX(), -1, pos.getZ())));
    }

    public int getCount() {
        return this.elements.size();
    }

    public void clear() {
        this.elements.clear();
    }

    public boolean isValid(Level world, T element) {
        return this.verifier.test(world, element.getPos(), world.getBlockState(element.getPos()));
    }

    @Nullable
    public T getRandomElement() {
        return MiscUtils.getRandomEntry(this.elements, rand);
    }

    @Nullable
    public T getRandomElementChanced() {
        if (this.elements.isEmpty()) {
            return null;
        }
        float perc = 1F - (((float) this.getCount()) / this.maxAmount);
        perc = 0.1F / ((perc / 2F) + 0.1F);
        if (rand.nextFloat() < perc) {
            return getRandomElement();
        }
        return null;
    }

    @Nonnull
    public Either<T, BlockPos> peekNewPosition(Level world, BlockPos pos, ConstellationEffectProperties prop) {
        if (this.excludesRitual || this.excludeRitualColumn) {
            TileRitualLink link = MiscUtils.getTileAt(world, pos, TileRitualLink.class, false);
            TileRitualPedestal pedestal = getPedestal(world, pos);
            this.isLinkedRitual = (pedestal != null && link != null && pos.equals(link.getLinkedTo()));
        }
        BlockPositionGenerator gen = this.selectPositionStrategy(this.positionStrategy, prop);
        if (gen != this.positionStrategy) {
            gen.copyFilterFrom(this.positionStrategy);
        }
        BlockPos at = gen.generateNextPosition(new Vector3(0.5, 0.5, 0.5), prop.getSize());
        BlockPos actual = at.offset(pos);

        if (this.getCount() >= this.maxAmount) {
            return Either.right(actual);
        }
        return MiscUtils.executeWithChunk(world, actual, () -> {
            if (this.verifier.test(world, actual, world.getBlockState(actual))) {
                T element = this.createElement(world, actual);
                if (element == null) {
                    return Either.right(actual);
                } else {
                    return Either.left(element);
                }
            }
            return Either.right(actual);
        }, Either.right(actual));
    }

    @Nonnull
    public Either<T, BlockPos> findNewPosition(Level world, BlockPos pos, ConstellationEffectProperties prop) {
        return this.peekNewPosition(world, pos, prop).ifLeft(entry -> {
            if (!this.hasElement(entry.getPos())) {
                this.elements.add(entry);
            }
        });
    }

    public boolean removeElement(T entry) {
        return removeElement(entry.getPos());
    }

    public boolean removeElement(BlockPos pos) {
        return this.elements.removeIf(e -> e.getPos().equals(pos));
    }

    public boolean hasElement(BlockPos pos) {
        return MiscUtils.contains(this.elements, e -> e.getPos().equals(pos));
    }

    @Override
    public void readFromNBT(CompoundTag cmp) {
        super.readFromNBT(cmp);
        this.elements.clear();
        ListTag list = cmp.getList("elements", Tag.TAG_COMPOUND);
        for (Tag nbt : list) {
            CompoundTag tag = (CompoundTag) nbt;
            BlockPos pos = NBTHelper.readBlockPosFromNBT(tag);
            CompoundTag tagData = tag.getCompound("data");
            T element = this.recreateElement(tagData, pos);
            if (element != null) {
                element.readFromNBT(tagData);
                this.elements.add(element);
            }
        }
    }

    @Override
    public void writeToNBT(CompoundTag cmp) {
        super.writeToNBT(cmp);
        ListTag list = new ListTag();
        for (T element : this.elements) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(element.getPos(), tag);
            CompoundTag dataTag = new CompoundTag();
            element.writeToNBT(dataTag);
            tag.put("data", dataTag);
            list.add(tag);
        }
        cmp.put("elements", list);
    }

    public static interface ListEntry {

        BlockPos getPos();

        void writeToNBT(CompoundTag nbt);

        void readFromNBT(CompoundTag nbt);
    }

    public static class CountConfig extends Config {

        private final int defaultMaxAmount;

        public ForgeConfigSpec.IntValue maxAmount;

        public CountConfig(String constellationName, double defaultRange, double defaultRangePerLens, int defaultMaxAmount) {
            super(constellationName, defaultRange, defaultRangePerLens);
            this.defaultMaxAmount = defaultMaxAmount;
        }

        @Override
        public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);
            this.maxAmount = cfgBuilder
                    .comment("Defines the amount of blocks this ritual will try to capture at most.")
                    .translation(translationKey("maxAmount"))
                    .defineInRange("maxAmount", this.defaultMaxAmount, 1, 2048);
        }
    }
}
