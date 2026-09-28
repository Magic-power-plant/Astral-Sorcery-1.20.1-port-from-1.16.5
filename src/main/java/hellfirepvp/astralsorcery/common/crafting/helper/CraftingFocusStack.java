package hellfirepvp.astralsorcery.common.crafting.helper;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a relay input slot during altar crafting.
 * Tracks which relay position is providing which ingredient for the recipe.
 */
public class CraftingFocusStack {

    private final int stackIndex;
    private final WrappedIngredient input;
    private final BlockPos realPosition;

    public CraftingFocusStack(int stackIndex, @Nullable WrappedIngredient input, BlockPos realPosition) {
        this.stackIndex = stackIndex;
        this.input = input;
        this.realPosition = realPosition;
    }

    public CraftingFocusStack(CompoundTag tag) {
        this.stackIndex = tag.getInt("stackIndex");
        this.realPosition = new BlockPos(
            tag.getInt("posX"),
            tag.getInt("posY"),
            tag.getInt("posZ")
        );
        this.input = WrappedIngredient.deserialize(tag);
    }

    public int getStackIndex() {
        return stackIndex;
    }

    @Nullable
    public WrappedIngredient getInput() {
        return input;
    }

    @Nonnull
    public BlockPos getRealPosition() {
        return realPosition;
    }

    @Nonnull
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("stackIndex", stackIndex);
        tag.putInt("posX", realPosition.getX());
        tag.putInt("posY", realPosition.getY());
        tag.putInt("posZ", realPosition.getZ());
        if (input != null) {
            tag.merge(input.serialize());
        }
        return tag;
    }
}
