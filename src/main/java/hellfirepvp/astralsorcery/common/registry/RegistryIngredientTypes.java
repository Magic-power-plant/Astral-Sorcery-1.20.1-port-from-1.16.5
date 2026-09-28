/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.CrystalIngredientSerializer;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredientSerializer;
import hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS.CRYSTAL_SERIALIZER;
import static hellfirepvp.astralsorcery.common.lib.IngredientSerializersAS.FLUID_SERIALIZER;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryIngredientTypes
 * Created by HellFirePvP
 * Date: 30.05.2019 / 17:48
 */
// Port notes for 1.16.5 common/registry/RegistryIngredientTypes.java (38 lines):
//
// init() (1.16.5 ref:30-36) is ported verbatim - both of its halves were missing from the 1.20.1 tree:
// common/lib/IngredientSerializersAS declares FLUID_SERIALIZER and CRYSTAL_SERIALIZER but never assigns them
// (they were null), and no CraftingHelper.register call existed anywhere, so
// common/crafting/helper/ingredient/CrystalIngredient#getSerializer returned null and its #toJson NPE'd inside
// CraftingHelper.getID. Both fields are plain public static (not final) in IngredientSerializersAS exactly as in
// 1.16.5, so the assignment below is the reference assignment, not a workaround.
//
//   CraftingHelper.register(ResourceLocation, IIngredientSerializer<T extends Ingredient>)
//                    -> unchanged in 1.20.1 (net.minecraftforge.common.crafting.CraftingHelper). It is a static
//                       BiMap put that throws IllegalStateException both on a duplicate id and on a duplicate
//                       serializer instance, hence the re-entry guard: if common/CommonProxy (outside this slice)
//                       ever gains the 1.16.5 RegistryIngredientTypes.init() call (1.16.5 CommonProxy:168), this
//                       class must not put the same two ids in a second time.
//   Ingredient#getSerializer / the "type" key in recipe json
//                    -> unchanged too; the two ids stay "astralsorcery:fluid" and "astralsorcery:crystal", so
//                       every recipe json shipped for 1.16.5 keeps resolving.
//
// 1.16.5 ran this from CommonProxy#initialize (ref: CommonProxy:168), i.e. during mod construction. The 1.20.1
// CommonProxy#initialize has no such call, so the class subscribes itself to the mod bus and registers from
// FMLCommonSetupEvent instead - still well before any datapack/recipe deserialization, which is the only thing
// that reads the ingredient BiMap, and before the first ClientPacketListener recipe sync on the client.
@Mod.EventBusSubscriber(modid = AstralSorcery.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryIngredientTypes {

    private static boolean initialized = false;

    private RegistryIngredientTypes() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(RegistryIngredientTypes::init);
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        IngredientSerializersAS.FLUID_SERIALIZER = new FluidIngredientSerializer();
        IngredientSerializersAS.CRYSTAL_SERIALIZER = new CrystalIngredientSerializer();

        CraftingHelper.register(AstralSorcery.key("fluid"), FLUID_SERIALIZER);
        CraftingHelper.register(AstralSorcery.key("crystal"), CRYSTAL_SERIALIZER);
    }
}
