package hellfirepvp.astralsorcery.common.item.crystal;

import hellfirepvp.astralsorcery.common.crystal.CrystalAttributeGenItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.entity.item.EntityCrystal;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

/**
 * Port of 1.16.5 ItemCrystalBase.
 * 1.16.5 applied Properties#maxDamage(0), which also forces the max stack size to 1.
 * Item.Properties#durability(int) has the same semantics in 1.20.1, so crystals stay unstackable.
 */
public abstract class ItemCrystalBase extends Item implements CrystalAttributeGenItem {

    public ItemCrystalBase(Properties properties) {
        super(properties.durability(0));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        if (!world.isClientSide()) {
            CrystalAttributes attributes = getAttributes(stack);

            if (attributes == null && stack.getItem() instanceof CrystalAttributeGenItem) {
                attributes = CrystalGenerator.generateNewAttributes(stack);
                attributes.store(stack);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> toolTip, TooltipFlag flag) {
        this.addCrystalPropertyToolTip(stack, toolTip);
    }

    @OnlyIn(Dist.CLIENT)
    protected CrystalAttributes.TooltipResult addCrystalPropertyToolTip(ItemStack stack, List<Component> tooltip) {
        CrystalAttributes attr = getAttributes(stack);
        if (attr != null) {
            return attr.addTooltip(tooltip);
        }
        return CrystalAttributes.TooltipResult.ALL_MISSING;
    }

    @Nullable
    @Override
    public CrystalAttributes getAttributes(ItemStack stack) {
        return CrystalAttributes.getCrystalAttributes(stack);
    }

    @Override
    public void setAttributes(ItemStack stack, @Nullable CrystalAttributes attributes) {
        if (attributes != null) {
            attributes.store(stack);
        } else {
            CrystalAttributes.storeNull(stack);
        }
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        EntityCrystal res = new EntityCrystal(EntityTypesAS.ITEM_CRYSTAL.get(), world, location.getX(), location.getY(), location.getZ(), itemstack);
        res.load(location.saveWithoutId(new CompoundTag()));
        res.applyColor(this.getItemEntityColor(itemstack));
        if (location instanceof ItemEntity) {
            res.setReplacedEntity((ItemEntity) location);
        }
        return res;
    }

    @Override
    public int getGeneratedPropertyTiers() {
        return 5;
    }

    @Override
    public int getMaxPropertyTiers() {
        return 7;
    }

    protected Color getItemEntityColor(ItemStack stack) {
        return ColorsAS.ROCK_CRYSTAL;
    }

    public abstract ItemAttunedCrystalBase getTunedItemVariant();

    public abstract ItemCrystalBase getInertDuplicateItem();

}
