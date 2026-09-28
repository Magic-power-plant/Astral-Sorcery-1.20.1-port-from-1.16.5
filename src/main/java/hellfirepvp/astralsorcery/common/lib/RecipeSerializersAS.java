/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.DyeableChangeColorRecipe;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeSerializersAS
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:32
 */
//TODO_PORT[AS-LIB-RS-01]: 1.16.5 declares the seven serializer fields below as plain mutable statics of the
// concrete serializer types (WellRecipeSerializer, LiquidInfusionSerializer, BlockTransmutationSerializer,
// SimpleAltarRecipeSerializer, LiquidInteractionSerializer and the two RecipeDyeableChangeColor inner
// serializers) and fills them in common/registry/RegistryRecipeSerializers#registerSerializers, where each
// serializer takes its registry name from the matching ResourceLocation constant here
// (1.16.5 common/lib/RecipeSerializersAS.java:38-45, common/crafting/serializer/*.java).
// The 1.20.1 tree restructured that completely: common/crafting/RecipeSerializersAS registers every
// serializer through a DeferredRegister under the very same ids, the serializer classes were renamed/nested
// (AltarRecipeSerializer, WellLiquefaction.Serializer, LiquidInfusion.Serializer, LiquidInteraction.Serializer,
// BlockTransmutation.Serializer, DyeableChangeColorRecipe.WandColorSerializer/.GatewayColorSerializer) and
// Forge's IForgeRegistryEntry#setRegistryName is gone, so the ids are passed to DeferredRegister#register
// instead of being read from this class. common/crafting/** is outside this slice, so this holder keeps the
// 1.16.5 names alive by aliasing that DeferredRegister; the RegistryObject wrapper and the serializer types
// are the parts that cannot be made identical to 1.16.5.
public class RecipeSerializersAS {

    private RecipeSerializersAS() {}

    public static final ResourceLocation WELL_LIQUEFACTION = AstralSorcery.key("lightwell");
    public static final ResourceLocation LIQUID_INFUSION = AstralSorcery.key("infuser");
    public static final ResourceLocation BLOCK_TRANSMUTATION = AstralSorcery.key("block_transmutation");
    public static final ResourceLocation SIMPLE_ALTAR_CRAFTING = AstralSorcery.key("altar");
    public static final ResourceLocation LIQUID_INTERACTION = AstralSorcery.key("liquid_interaction");

    public static final ResourceLocation CUSTOM_CHANGE_WAND_COLOR = AstralSorcery.key("change_wand_color");
    public static final ResourceLocation CUSTOM_CHANGE_GATEWAY_COLOR = AstralSorcery.key("change_gateway_color");

    public static final RegistryObject<RecipeSerializer<WellLiquefaction>> WELL_LIQUEFACTION_SERIALIZER =
            hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS.LIGHTWELL;
    public static final RegistryObject<RecipeSerializer<LiquidInfusion>> LIQUID_INFUSION_SERIALIZER =
            hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS.INFUSER;
    public static final RegistryObject<RecipeSerializer<BlockTransmutation>> BLOCK_TRANSMUTATION_SERIALIZER =
            hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS.BLOCK_TRANSMUTATION;
    public static final RegistryObject<RecipeSerializer<AltarRecipe>> ALTAR_RECIPE_SERIALIZER =
            hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS.ALTAR;
    public static final RegistryObject<RecipeSerializer<LiquidInteraction>> LIQUID_INTERACTION_SERIALIZER =
            hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS.LIQUID_INTERACTION;

    public static final RegistryObject<RecipeSerializer<DyeableChangeColorRecipe>> CUSTOM_CHANGE_WAND_COLOR_SERIALIZER =
            hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS.CHANGE_WAND_COLOR;
    public static final RegistryObject<RecipeSerializer<DyeableChangeColorRecipe>> CUSTOM_CHANGE_GATEWAY_COLOR_SERIALIZER =
            hellfirepvp.astralsorcery.common.crafting.RecipeSerializersAS.CHANGE_GATEWAY_COLOR;

}
