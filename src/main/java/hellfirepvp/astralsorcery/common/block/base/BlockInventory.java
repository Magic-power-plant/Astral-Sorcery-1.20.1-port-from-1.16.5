package hellfirepvp.astralsorcery.common.block.base;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public abstract class BlockInventory extends BlockCrystalContainer {

    protected BlockInventory(Properties builder) {
        super(builder);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity te = MiscUtils.getTileAt(worldIn, pos, BlockEntity.class, true);
        if (te != null && !worldIn.isClientSide) {
            LazyOptional<IItemHandler> opt = te.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if (opt.isPresent()) {
                ItemUtils.dropInventory(opt.orElse(null), worldIn, pos);
            }
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

}
