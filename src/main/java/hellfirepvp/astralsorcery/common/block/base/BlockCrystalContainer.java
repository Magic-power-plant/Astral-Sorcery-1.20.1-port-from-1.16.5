package hellfirepvp.astralsorcery.common.block.base;

import hellfirepvp.astralsorcery.api.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.ConstellationBlockEntity;
import hellfirepvp.astralsorcery.api.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public abstract class BlockCrystalContainer extends BaseEntityBlock {

    protected BlockCrystalContainer(Properties builder) {
        super(builder);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, world, pos, player);
        if (stack.getItem() instanceof CrystalAttributeItem) {
            CrystalAttributeBlockEntity cat = MiscUtils.getBlockEntityAt(world, pos, CrystalAttributeBlockEntity.class, true);
            if (cat != null) {
                ((CrystalAttributeItem) stack.getItem()).setAttributes(stack, cat.getAttributes());
            }
        }
        if (stack.getItem() instanceof ConstellationItem) {
            ConstellationBlockEntity ct = MiscUtils.getBlockEntityAt(world, pos, ConstellationBlockEntity.class, true);
            if (ct != null) {
                ((ConstellationItem) stack.getItem()).setAttunedConstellation(stack, ct.getAttunedConstellation());
                ((ConstellationItem) stack.getItem()).setTraitConstellation(stack, ct.getTraitConstellation());
            }
        }
        return stack;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Item i = stack.getItem();
        if (i instanceof CrystalAttributeItem) {
            CrystalAttributeBlockEntity cat = MiscUtils.getBlockEntityAt(world, pos, CrystalAttributeBlockEntity.class, true);
            if (cat != null) {
                cat.setAttributes(((CrystalAttributeItem) i).getAttributes(stack));
            }
        }
        if (i instanceof ConstellationItem) {
            ConstellationBlockEntity ct = MiscUtils.getBlockEntityAt(world, pos, ConstellationBlockEntity.class, true);
            if (ct != null) {
                ct.setAttunedConstellation(((ConstellationItem) i).getAttunedConstellation(stack));
                ct.setTraitConstellation(((ConstellationItem) i).getTraitConstellation(stack));
            }
        }
    }

}
