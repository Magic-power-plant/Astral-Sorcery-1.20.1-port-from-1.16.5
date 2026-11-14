/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.registry;

import hellfirepvp.astralsorcery.common.lib.FluidsAS;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.ChunkRenderTypeSet;

import static hellfirepvp.astralsorcery.common.lib.BlocksAS.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryBlockRenderTypes
 * Created by HellFirePvP
 * Date: 10.06.2020 / 21:06
 */
// Port notes for 1.16.5 common/registry/RegistryBlockRenderTypes.java (121 lines):
//
//   RenderTypeLookup                      -> net.minecraft.client.renderer.ItemBlockRenderTypes (renamed in 1.17).
//   RenderType.getSolid/getCutout/getTranslucent -> RenderType.solid()/cutout()/translucent().
//   setRenderLayer(Block, Predicate<RenderType>) -> still present in Forge 1.20.1, and so is a
//                                            setRenderLayer(Block, ChunkRenderTypeSet) overload that the other two
//                                            delegate to. All three are deprecated for removal in favour of the
//                                            model json "render_type" field, but ChunkRenderTypeSet is the only
//                                            remaining code-side way to keep the 1.16.5 behaviour of a block
//                                            rendering in *several* chunk layers (solid + translucent), which the
//                                            single-RenderType overload cannot express.
//   setRenderLayer(Fluid, Predicate<RenderType>) -> Forge dropped the fluid predicate overload; a fluid has exactly
//                                            one layer now, so setRenderLayer(Fluid, RenderType) is used.
//   RegistryFluids.FLUID_BLOCKS (ref:106)  -> common/lib/FluidsAS#LIQUID_STARLIGHT_BLOCK, the only entry that list
//                                            ever held (see the port notes on RegistryFluids).
//   GATEWAY (ref:93)                       -> BlocksAS.CELESTIAL_GATEWAY (renamed in the 1.20.1 tree).
//
// 1.16.5 registered TELESCOPE twice (ref:87 and ref:88, identical lines); it is registered once here.
public class RegistryBlockRenderTypes {

    private RegistryBlockRenderTypes() {}

    public static void initBlocks() {
        setRenderLayer(MARBLE_ARCH.get(),           RenderType.solid());
        setRenderLayer(MARBLE_BRICKS.get(),         RenderType.solid());
        setRenderLayer(MARBLE_CHISELED.get(),       RenderType.solid());
        setRenderLayer(MARBLE_ENGRAVED.get(),       RenderType.solid());
        setRenderLayer(MARBLE_PILLAR.get(),         RenderType.solid());
        setRenderLayer(MARBLE_RAW.get(),            RenderType.solid());
        setRenderLayer(MARBLE_RUNED.get(),          RenderType.solid());
        setRenderLayer(MARBLE_STAIRS.get(),         RenderType.solid());
        setRenderLayer(MARBLE_SLAB.get(),           RenderType.solid());
        setRenderLayer(BLACK_MARBLE_ARCH.get(),     RenderType.solid());
        setRenderLayer(BLACK_MARBLE_BRICKS.get(),   RenderType.solid());
        setRenderLayer(BLACK_MARBLE_CHISELED.get(), RenderType.solid());
        setRenderLayer(BLACK_MARBLE_ENGRAVED.get(), RenderType.solid());
        setRenderLayer(BLACK_MARBLE_PILLAR.get(),   RenderType.solid());
        setRenderLayer(BLACK_MARBLE_RAW.get(),      RenderType.solid());
        setRenderLayer(BLACK_MARBLE_RUNED.get(),    RenderType.solid());
        setRenderLayer(BLACK_MARBLE_STAIRS.get(),   RenderType.solid());
        setRenderLayer(BLACK_MARBLE_SLAB.get(),     RenderType.solid());
        setRenderLayer(INFUSED_WOOD.get(),          RenderType.solid());
        setRenderLayer(INFUSED_WOOD_ARCH.get(),     RenderType.solid());
        setRenderLayer(INFUSED_WOOD_COLUMN.get(),   RenderType.solid());
        setRenderLayer(INFUSED_WOOD_ENGRAVED.get(), RenderType.solid());
        setRenderLayer(INFUSED_WOOD_ENRICHED.get(), RenderType.solid());
        setRenderLayer(INFUSED_WOOD_INFUSED.get(),  RenderType.solid());
        setRenderLayer(INFUSED_WOOD_PLANKS.get(),   RenderType.solid());
        setRenderLayer(INFUSED_WOOD_STAIRS.get(),   RenderType.solid());
        setRenderLayer(INFUSED_WOOD_SLAB.get(),     RenderType.solid());

        setRenderLayer(AQUAMARINE_SAND_ORE.get(), RenderType.solid(), RenderType.translucent());
        setRenderLayer(ROCK_CRYSTAL_ORE.get(),    RenderType.solid(), RenderType.translucent());
        setRenderLayer(STARMETAL_ORE.get(),       RenderType.solid(), RenderType.translucent());
        setRenderLayer(STARMETAL.get(),           RenderType.solid());
        setRenderLayer(GLOW_FLOWER.get(),         RenderType.cutout());

        setRenderLayer(SPECTRAL_RELAY.get(),              RenderType.solid(), RenderType.translucent());
        setRenderLayer(ALTAR_DISCOVERY.get(),             RenderType.solid());
        setRenderLayer(ALTAR_ATTUNEMENT.get(),            RenderType.solid());
        setRenderLayer(ALTAR_CONSTELLATION.get(),         RenderType.solid());
        setRenderLayer(ALTAR_RADIANCE.get(),              RenderType.solid());
        setRenderLayer(ATTUNEMENT_ALTAR.get(),            RenderType.solid());
        setRenderLayer(CELESTIAL_CRYSTAL_CLUSTER.get(),   RenderType.solid(), RenderType.translucent());
        setRenderLayer(GEM_CRYSTAL_CLUSTER.get(),         RenderType.solid(), RenderType.translucent());
        setRenderLayer(ROCK_COLLECTOR_CRYSTAL.get(),      RenderType.solid(), RenderType.translucent());
        setRenderLayer(CELESTIAL_COLLECTOR_CRYSTAL.get(), RenderType.solid(), RenderType.translucent());
        setRenderLayer(LENS.get(),                        RenderType.solid(), RenderType.translucent());
        setRenderLayer(PRISM.get(),                       RenderType.solid(), RenderType.translucent());
        setRenderLayer(RITUAL_LINK.get(),                 RenderType.solid(), RenderType.translucent());
        setRenderLayer(RITUAL_PEDESTAL.get(),             RenderType.solid(), RenderType.translucent());
        setRenderLayer(INFUSER.get(),                     RenderType.solid(), RenderType.translucent());
        setRenderLayer(CHALICE.get(),                     RenderType.solid());
        setRenderLayer(WELL.get(),                        RenderType.solid());
        setRenderLayer(ILLUMINATOR.get(),                 RenderType.solid(), RenderType.translucent());
        setRenderLayer(TELESCOPE.get(),                   RenderType.solid());
        setRenderLayer(OBSERVATORY.get(),                 RenderType.solid());
        setRenderLayer(REFRACTION_TABLE.get(),            RenderType.solid());
        setRenderLayer(TREE_BEACON.get(),                 RenderType.solid());
        setRenderLayer(TREE_BEACON_COMPONENT.get(),       RenderType.translucent());
        setRenderLayer(CELESTIAL_GATEWAY.get(),           RenderType.solid(), RenderType.translucent());
        setRenderLayer(FOUNTAIN.get(),                    RenderType.solid());
        setRenderLayer(FOUNTAIN_PRIME_LIQUID.get(),       RenderType.solid(), RenderType.translucent());
        setRenderLayer(FOUNTAIN_PRIME_VORTEX.get(),       RenderType.solid(), RenderType.translucent());
        setRenderLayer(FOUNTAIN_PRIME_ORE.get(),          RenderType.solid(), RenderType.translucent());

        setRenderLayer(FLARE_LIGHT.get(),       RenderType.translucent());
        setRenderLayer(TRANSLUCENT_BLOCK.get(), RenderType.translucent());
        setRenderLayer(VANISHING.get(),         RenderType.translucent());
        setRenderLayer(STRUCTURAL.get(),        RenderType.translucent());
    }

    public static void initFluids() {
        setRenderLayer(FluidsAS.LIQUID_STARLIGHT_BLOCK.get(), RenderType.translucent());

        setRenderLayer(FluidsAS.LIQUID_STARLIGHT_SOURCE.get(), RenderType.translucent());
        setRenderLayer(FluidsAS.LIQUID_STARLIGHT_FLOWING.get(), RenderType.translucent());
    }

    @SuppressWarnings("removal")
    private static void setRenderLayer(Block block, RenderType... types) {
        // 1.16.5: RenderTypeLookup.setRenderLayer(block, Arrays.asList(types)::contains)
        // All three ItemBlockRenderTypes#setRenderLayer(Block, ..) overloads are @Deprecated(forRemoval = true) in
        // Forge 1.20.1 - the preferred route is the "render_type" field in the block model json - but the
        // ChunkRenderTypeSet overload is the one the other two delegate to and the only code-side way to express
        // the multi-layer registrations above.
        ItemBlockRenderTypes.setRenderLayer(block, ChunkRenderTypeSet.of(types));
    }

    private static void setRenderLayer(Fluid fluid, RenderType type) {
        ItemBlockRenderTypes.setRenderLayer(fluid, type);
    }
}
