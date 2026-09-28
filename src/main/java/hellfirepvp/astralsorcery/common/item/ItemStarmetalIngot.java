/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item;

import hellfirepvp.astralsorcery.common.entity.item.EntityStarmetal;
import hellfirepvp.astralsorcery.common.lib.EntityTypesAS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemStarmetalIngot
 * Created by HellFirePvP
 * Date: 21.07.2019 / 12:24
 */
public class ItemStarmetalIngot extends Item {

    // 1.16.5 built its own Properties here with group(CommonProxy.ITEM_GROUP_AS).
    // 1.20.1 has no Properties#group, so the properties are supplied by ItemsAS
    // and the creative tab is applied centrally.
    public ItemStarmetalIngot(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(Level world, Entity location, ItemStack itemstack) {
        // 1.16.5 Entity#getPosX/Y/Z -> getX/Y/Z; Entity#writeWithoutTypeId -> saveWithoutId; Entity#read -> load
        EntityStarmetal res = new EntityStarmetal(EntityTypesAS.ITEM_STARMETAL_INGOT.get(), world, location.getX(), location.getY(), location.getZ(), itemstack);
        res.load(location.saveWithoutId(new CompoundTag()));
        if (location instanceof ItemEntity) {
            res.setReplacedEntity((ItemEntity) location);
        }
        return res;
    }
}
