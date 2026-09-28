/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.block.base;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CustomItemBlockProperties
 * Created by HellFirePvP
 * Date: 01.06.2019 / 14:23
 */
public interface CustomItemBlockProperties extends CustomItemBlock {

    default int getItemMaxStackSize() {
        return getItemMaxDamage() > 0 ? 1 : 64;
    }

    default int getItemMaxDamage() {
        return 0;
    }

    @Nullable
    default Item getContainerItem() {
        return null;
    }

    //1.16.5: ItemGroup -> 1.20.1: CreativeModeTab. 1.20.1 fills creative tabs through
    //BuildCreativeModeTabContentsEvent instead of Item.Properties#group, so the consumer of this value changed;
    //the accessor itself is unchanged.
    @Nullable
    default CreativeModeTab getItemGroup() {
        return null;
    }

    @Nonnull
    default Rarity getItemRarity() {
        return Rarity.COMMON;
    }

    default boolean canItemBeRepaired() {
        return false;
    }

    //TODO_PORT[AS-BLK-12]: 1.16.5 also declares
    //`@Nonnull default Map<ToolType, Integer> getItemToolLevels() { return Collections.emptyMap(); }`
    //(1.16.5 CustomItemBlockProperties.java:62-65), consumed by RegistryItems#buildItemBlockProperties through
    //Item.Properties#addToolType. Forge removed net.minecraftforge.common.ToolType (and Item.Properties#addToolType)
    //in 1.18 in favour of block/item tags, so there is no 1.20.1 expression of this accessor. No 1.16.5 block
    //overrides it - every caller got the empty map - so dropping it changes no behaviour.

    //1.16.5: ItemStackTileEntityRenderer -> 1.20.1: BlockEntityWithoutLevelRenderer. 1.20.1 wires the custom
    //renderer through IClientItemExtensions#getCustomRenderer rather than Item.Properties#setISTER; the accessor
    //keeps the reference's shape (no 1.16.5 block overrides it either).
    @Nullable
    default Supplier<Callable<BlockEntityWithoutLevelRenderer>> getItemTEISR() {
        return null;
    }

}
