/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.common.lib.RecipeSerializersAS;
import hellfirepvp.astralsorcery.common.block.BlockCelestialGateway;
import hellfirepvp.astralsorcery.common.item.wand.ItemIlluminationWand;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeDyeableChangeColor
 * Created by HellFirePvP
 * Date: 29.11.2019 / 13:24
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 calls this class RecipeDyeableChangeColor and keeps it in
//common/crafting/custom; this tree has it as common/crafting/DyeableChangeColorRecipe and it is the class
//RecipeSerializersAS actually registers ("change_wand_color" / "change_gateway_color"), so the name and
//location stay - renaming it would touch common/lib/RecipeSerializersAS, which is outside this slice.
//Note: this tree also carries an unregistered common/crafting/custom/RecipeDyeableChangeColor whose two
//nested serializers return null from fromJson/fromNetwork; it is dead code that nothing references. That
//file is outside this slice - see externalChangesNeeded.
//The matching/assembling logic below was missing entirely: matches(...) returned false and assemble(...)
//returned ItemStack.EMPTY, so dyeing an illumination wand or a celestial gateway silently did nothing
//(1.16.5 common/crafting/custom/RecipeDyeableChangeColor.java:53-95).
//1.16.5 API translation: SpecialRecipe -> CustomRecipe (which added the CraftingBookCategory constructor
//parameter and already returns ItemStack.EMPTY from getResultItem), SpecialRecipeSerializer ->
//SimpleCraftingRecipeSerializer, CraftingInventory -> CraftingContainer, #getSizeInventory ->
//#getContainerSize, #getStackInSlot -> #getItem, #getCraftingResult -> #assemble, #canFit ->
//#canCraftInDimensions.
public class DyeableChangeColorRecipe extends CustomRecipe {

    private final Supplier<RecipeSerializer<?>> serializer;
    //1.16.5 stored the target as a plain Item because ItemsAS/BlocksAS held raw Item/Block instances there.
    //This tree's lib holders are RegistryObjects, and the serializer is constructed during the recipe
    //serializer registry event, so the item is resolved lazily instead of at construction time.
    private final Supplier<Item> targetItem;
    private final BiConsumer<ItemStack, DyeColor> colorFn;

    public DyeableChangeColorRecipe(ResourceLocation idIn, CraftingBookCategory category, Supplier<RecipeSerializer<?>> serializer,
                                    Supplier<Item> targetItem, BiConsumer<ItemStack, DyeColor> colorFn) {
        super(idIn, category);
        this.serializer = serializer;
        this.targetItem = targetItem;
        this.colorFn = colorFn;
    }

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        return tryFindValidRecipeAndDye(inv) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        Tuple<DyeColor, ItemStack> itemColorTpl = tryFindValidRecipeAndDye(inv);
        if (itemColorTpl == null) {
            return ItemStack.EMPTY;
        }
        ItemStack out = ItemUtils.copyStackWithSize(itemColorTpl.getB(), 1);
        this.colorFn.accept(out, itemColorTpl.getA());
        return out;
    }

    @Nullable
    private Tuple<DyeColor, ItemStack> tryFindValidRecipeAndDye(CraftingContainer inv) {
        ItemStack itemFound = ItemStack.EMPTY;
        DyeColor dyeColorFound = null;
        int nonEmptyItemsFound = 0;

        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            ItemStack in = inv.getItem(slot);
            if (!in.isEmpty()) {
                nonEmptyItemsFound++;

                if (in.getItem().equals(this.targetItem.get())) {
                    itemFound = in;
                } else {
                    DyeColor color = DyeColor.getColor(in);
                    if (color != null) {
                        dyeColorFound = color;
                    }
                }
            }
        }

        if (itemFound.isEmpty() || dyeColorFound == null || nonEmptyItemsFound != 2) {
            return null;
        } else {
            return new Tuple<>(dyeColorFound, itemFound);
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer.get();
    }

    //1.16.5 names these IlluminationWandColorSerializer and CelestialGatewayColorSerializer and ends each
    //constructor with "this.setRegistryName(RecipeSerializersAS.CUSTOM_CHANGE_*_COLOR);"; registry names are
    //supplied by DeferredRegister#register in 1.20.1, so that call has no 1.20.1 equivalent. The class names
    //stay as this tree has them because common/lib/RecipeSerializersAS refers to them by these names.
    public static class WandColorSerializer extends SimpleCraftingRecipeSerializer<DyeableChangeColorRecipe> {
        public WandColorSerializer() {
            super((id, category) -> new DyeableChangeColorRecipe(id, category,
                    () -> RecipeSerializersAS.CHANGE_WAND_COLOR.get(),
                    () -> ItemsAS.ILLUMINATION_WAND.get(), ItemIlluminationWand::setConfiguredColor));
        }
    }

    public static class GatewayColorSerializer extends SimpleCraftingRecipeSerializer<DyeableChangeColorRecipe> {
        public GatewayColorSerializer() {
            //1.16.5: Item.getItemFromBlock(BlocksAS.GATEWAY) - the block is CELESTIAL_GATEWAY in this tree
            //and Block#asItem replaces Item#getItemFromBlock.
            super((id, category) -> new DyeableChangeColorRecipe(id, category,
                    () -> RecipeSerializersAS.CHANGE_GATEWAY_COLOR.get(),
                    () -> BlocksAS.CELESTIAL_GATEWAY.get().asItem(), BlockCelestialGateway::setColor));
        }
    }
}
