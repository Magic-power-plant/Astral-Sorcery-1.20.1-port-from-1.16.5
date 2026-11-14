package hellfirepvp.astralsorcery.common.blockentity.altar;

import hellfirepvp.astralsorcery.common.data.research.ResearchProgression;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public enum AltarType {

    DISCOVERY(ResearchProgression.BASIC_CRAFT, 100, 1, () -> BlocksAS.ALTAR_DISCOVERY.get().asItem(), () -> StructureTypesAS.EMPTY, 6, 7, 8, 11, 12, 13, 16, 17, 18),
    ATTUNEMENT(ResearchProgression.ATTUNEMENT, 200, 2, () -> BlocksAS.ALTAR_ATTUNEMENT.get().asItem(), () -> StructureTypesAS.PTYPE_ALTAR_ATTUNEMENT, 0, 4, 6, 7, 8, 11, 12, 13, 16, 17, 18, 20, 24),
    CONSTELLATION(ResearchProgression.CONSTELLATION, 400, 2, () -> BlocksAS.ALTAR_CONSTELLATION.get().asItem(), () -> StructureTypesAS.PTYPE_ALTAR_CONSTELLATION, slot -> slot != 2 && slot != 10 && slot != 14 && slot != 22),
    RADIANCE(ResearchProgression.RADIANCE, 600, 3, () -> BlocksAS.ALTAR_RADIANCE.get().asItem(), () -> StructureTypesAS.PTYPE_ALTAR_TRAIT, slot -> true);

    private final ResearchProgression associatedTier;
    private final int defaultAltarCraftingDuration;
    private final int minimumSources;
    private final Supplier<Item> altarItemSupplier;
    private final Supplier<StructureType> structureSupplier;
    private final Predicate<Integer> slotValidator;

    AltarType(ResearchProgression associatedTier, int defaultAltarCraftingDuration, int minimumSources, Supplier<Item> altarItemSupplier, Supplier<StructureType> structureSupplier, int... validSlots) {
        this(associatedTier, defaultAltarCraftingDuration, minimumSources, altarItemSupplier, structureSupplier, slotListPredicate(validSlots));
    }

    AltarType(ResearchProgression associatedTier, int defaultAltarCraftingDuration, int minimumSources, Supplier<Item> altarItemSupplier, Supplier<StructureType> structureSupplier, Predicate<Integer> slotValidator) {
        this.associatedTier = associatedTier;
        this.defaultAltarCraftingDuration = defaultAltarCraftingDuration;
        this.minimumSources = minimumSources;
        this.altarItemSupplier = altarItemSupplier;
        this.structureSupplier = structureSupplier;
        this.slotValidator = slotValidator;
    }

    private static Predicate<Integer> slotListPredicate(int... validSlots) {
        List<Integer> slots = new ArrayList<>();
        for (int slot : validSlots) {
            slots.add(slot);
        }
        return slots::contains;
    }

    @Nonnull
    public ResearchProgression getAssociatedTier() {
        return associatedTier;
    }

    @Nonnull
    public StructureType getRequiredStructure() {
        return structureSupplier.get();
    }

    @Nonnull
    public ItemStack getAltarItemRepresentation() {
        return new ItemStack(altarItemSupplier.get());
    }

    public boolean hasSlot(int slotId) {
        return slotValidator.test(slotId);
    }

    public int getStarlightCapacity() {
        return (int) (1000 * Math.pow(2, ordinal()));
    }

    public int getDefaultAltarCraftingDuration() {
        return defaultAltarCraftingDuration;
    }

    public int getMinimumSources() {
        return minimumSources;
    }

    public boolean isThisLEThan(AltarType type) {
        return this.ordinal() <= type.ordinal();
    }

    public boolean isThisGEThan(AltarType type) {
        return this.ordinal() >= type.ordinal();
    }
}
