/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeSerializersAS
 * Created by HellFirePvP
 * Date: 30.06.2019 / 23:32
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps the serializer holder at common/lib/RecipeSerializersAS, where it is a
//plain container of seven ResourceLocation ids plus seven mutable statics of the concrete serializer types,
//filled by common/registry/RegistryRecipeSerializers because every serializer carried its own registry name
//via IForgeRegistryEntry#setRegistryName. That API is gone in 1.20.1, so this tree registers the serializers
//through a DeferredRegister here instead and the ids are the DeferredRegister#register keys. The registered
//ids ("lightwell", "infuser", "block_transmutation", "altar", "liquid_interaction", "change_wand_color",
//"change_gateway_color") are byte-identical to 1.16.5's, so datapacks and existing worlds are unaffected.
//The 1.16.5 field names stay reachable through common/lib/RecipeSerializersAS, which aliases the entries
//below (see TODO_PORT[AS-LIB-RS-01] there); common/lib is outside this slice, so it is not touched here.
//The additional "well" id below has no 1.16.5 counterpart - 1.16.5 only ever registered "lightwell"
//(1.16.5 common/lib/RecipeSerializersAS.java:29). It is additive: the same serializer is bound twice, so the
//"lightwell" path behaves exactly as before. Removing it would break this tree's WellLiquefaction recipes,
//which is why the divergence is documented rather than reverted.
public class RecipeSerializersAS {

    private RecipeSerializersAS() {}

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AstralSorcery.MODID);

    public static final RegistryObject<RecipeSerializer<AltarRecipe>> ALTAR = RECIPE_SERIALIZERS.register("altar", AltarRecipeSerializer::new);
    public static final RegistryObject<RecipeSerializer<WellLiquefaction>> WELL = RECIPE_SERIALIZERS.register("well", WellLiquefaction.Serializer::new);
    public static final RegistryObject<RecipeSerializer<WellLiquefaction>> LIGHTWELL = RECIPE_SERIALIZERS.register("lightwell", WellLiquefaction.Serializer::new);
    public static final RegistryObject<RecipeSerializer<LiquidInfusion>> INFUSER = RECIPE_SERIALIZERS.register("infuser", LiquidInfusion.Serializer::new);
    public static final RegistryObject<RecipeSerializer<LiquidInteraction>> LIQUID_INTERACTION = RECIPE_SERIALIZERS.register("liquid_interaction", LiquidInteraction.Serializer::new);
    public static final RegistryObject<RecipeSerializer<BlockTransmutation>> BLOCK_TRANSMUTATION = RECIPE_SERIALIZERS.register("block_transmutation", BlockTransmutation.Serializer::new);
    public static final RegistryObject<RecipeSerializer<DyeableChangeColorRecipe>> CHANGE_WAND_COLOR = RECIPE_SERIALIZERS.register("change_wand_color", DyeableChangeColorRecipe.WandColorSerializer::new);
    public static final RegistryObject<RecipeSerializer<DyeableChangeColorRecipe>> CHANGE_GATEWAY_COLOR = RECIPE_SERIALIZERS.register("change_gateway_color", DyeableChangeColorRecipe.GatewayColorSerializer::new);
}
