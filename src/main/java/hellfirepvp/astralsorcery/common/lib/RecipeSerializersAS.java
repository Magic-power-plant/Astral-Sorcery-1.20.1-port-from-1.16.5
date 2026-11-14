/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipeSerializer;
import hellfirepvp.astralsorcery.common.crafting.BlockTransmutation;
import hellfirepvp.astralsorcery.common.crafting.DyeableChangeColorRecipe;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.crafting.LiquidInteraction;
import hellfirepvp.astralsorcery.common.crafting.WellLiquefaction;

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
public final class RecipeSerializersAS {

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
