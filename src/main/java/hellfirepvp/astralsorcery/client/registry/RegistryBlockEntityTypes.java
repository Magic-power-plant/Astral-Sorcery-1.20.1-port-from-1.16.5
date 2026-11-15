package hellfirepvp.astralsorcery.client.registry;

import hellfirepvp.astralsorcery.client.render.blockentity.RenderAltar;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderAttunementAltar;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderChalice;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderCollectorCrystal;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderInfuser;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderLens;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderObservatory;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderPrism;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderRefractionTable;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderRitualPedestal;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderSpectralRelay;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderTelescope;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderFakedState;
import hellfirepvp.astralsorcery.client.render.blockentity.RenderWell;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;

/** Client renderer bindings for the block entity types declared by BlockEntityTypesAS. */
@OnlyIn(Dist.CLIENT)
public final class RegistryBlockEntityTypes {

    private RegistryBlockEntityTypes() {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void initClient(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.ALTAR.get(), RenderAltar::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.ATTUNEMENT_ALTAR.get(), RenderAttunementAltar::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.CHALICE.get(), RenderChalice::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.COLLECTOR_CRYSTAL.get(), RenderCollectorCrystal::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.INFUSER.get(), RenderInfuser::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.LENS.get(), RenderLens::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.OBSERVATORY.get(), RenderObservatory::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.PRISM.get(), RenderPrism::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.REFRACTION_TABLE.get(), RenderRefractionTable::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.RITUAL_PEDESTAL.get(), RenderRitualPedestal::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.SPECTRAL_RELAY.get(), RenderSpectralRelay::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.TELESCOPE.get(), RenderTelescope::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.TRANSLUCENT_BLOCK.get(), RenderFakedState::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.TREE_BEACON_COMPONENT.get(), RenderFakedState::new);
        event.registerBlockEntityRenderer((BlockEntityType) BlockEntityTypesAS.WELL.get(), RenderWell::new);
    }
}
