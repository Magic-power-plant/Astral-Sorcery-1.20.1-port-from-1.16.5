package hellfirepvp.astralsorcery.common.registry;

import hellfirepvp.astralsorcery.client.render.tile.RenderAltar;
import hellfirepvp.astralsorcery.client.render.tile.RenderAttunementAltar;
import hellfirepvp.astralsorcery.client.render.tile.RenderChalice;
import hellfirepvp.astralsorcery.client.render.tile.RenderCollectorCrystal;
import hellfirepvp.astralsorcery.client.render.tile.RenderInfuser;
import hellfirepvp.astralsorcery.client.render.tile.RenderLens;
import hellfirepvp.astralsorcery.client.render.tile.RenderObservatory;
import hellfirepvp.astralsorcery.client.render.tile.RenderPrism;
import hellfirepvp.astralsorcery.client.render.tile.RenderRefractionTable;
import hellfirepvp.astralsorcery.client.render.tile.RenderRitualPedestal;
import hellfirepvp.astralsorcery.client.render.tile.RenderSpectralRelay;
import hellfirepvp.astralsorcery.client.render.tile.RenderTelescope;
import hellfirepvp.astralsorcery.client.render.tile.RenderTileFakedState;
import hellfirepvp.astralsorcery.client.render.tile.RenderWell;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryTileEntities
 * Created by HellFirePvP
 * Date: 01.06.2019 / 13:35
 */
// Port notes for 1.16.5 common/registry/RegistryTileEntities.java (99 lines):
//
//   registerTiles()        (1.16.5 ref:38-62) -> common/lib/TileEntityTypesAS, which holds all twenty-three
//                                                BlockEntityType entries as a DeferredRegister of
//                                                ForgeRegistries.BLOCK_ENTITY_TYPES. Repeating them here would
//                                                register every tile type twice.
//   registerTile(Class<T>, Block...)
//                          (ref:83-98)        -> TileEntityType.Builder.create(Supplier, Block...) is
//                                                BlockEntityType.Builder.of(BlockEntitySupplier, Block...), and
//                                                the supplier signature changed: 1.16.5 reflected a no-arg
//                                                constructor (tileClass.newInstance()), 1.20.1 tiles take
//                                                (BlockPos, BlockState), so TileEntityTypesAS passes the
//                                                Tile*::new method references directly. #setRegistryName and the
//                                                registry primer are gone (IForgeRegistryEntry deleted in 1.19);
//                                                the names 1.16.5 derived from NameUtil.fromClass(tileClass,
//                                                "Tile") are spelled out in the DeferredRegister#register calls.
//   initClient()           (ref:64-81)        -> kept below. ClientRegistry.bindTileEntityRenderer was removed in
//                                                1.17 together with net.minecraftforge.fml.client.registry;
//                                                block entity renderers are bound from the mod-bus
//                                                EntityRenderersEvent.RegisterRenderers, which
//                                                common/../client/ClientProxy adds this method as a listener for.
//                                                All fifteen bindings are present, in the reference's order.
//
// Two TileEntityTypesAS names do not match what NameUtil.fromClass produced in 1.16.5, and one type accepts an
// extra valid block; both live in common/lib and are reported as external changes:
//   TileCelestialCrystals -> 1.16.5 "astralsorcery:celestial_crystals", 1.20.1 "celestial_crystal_cluster"
//   TileGemCrystals       -> 1.16.5 "astralsorcery:gem_crystals",       1.20.1 "gem_crystal_cluster"
//   COLLECTOR_CRYSTAL     -> 1.16.5 valid blocks are ROCK_COLLECTOR_CRYSTAL and CELESTIAL_COLLECTOR_CRYSTAL only
//                            (ref:45), and the 1.20.1 holder now matches: the invented
//                            BlocksAS.COLLECTOR_CRYSTAL third entry has been removed.
public class RegistryTileEntities {

    private RegistryTileEntities() {}

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void initClient(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.ALTAR.get(), RenderAltar::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.ATTUNEMENT_ALTAR.get(), RenderAttunementAltar::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.CHALICE.get(), RenderChalice::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.COLLECTOR_CRYSTAL.get(), RenderCollectorCrystal::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.INFUSER.get(), RenderInfuser::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.LENS.get(), RenderLens::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.OBSERVATORY.get(), RenderObservatory::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.PRISM.get(), RenderPrism::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.REFRACTION_TABLE.get(), RenderRefractionTable::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.RITUAL_PEDESTAL.get(), RenderRitualPedestal::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.SPECTRAL_RELAY.get(), RenderSpectralRelay::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.TELESCOPE.get(), RenderTelescope::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.TRANSLUCENT_BLOCK.get(), RenderTileFakedState::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.TREE_BEACON_COMPONENT.get(), RenderTileFakedState::new);
        event.registerBlockEntityRenderer((BlockEntityType) TileEntityTypesAS.WELL.get(), RenderWell::new);
    }
}
